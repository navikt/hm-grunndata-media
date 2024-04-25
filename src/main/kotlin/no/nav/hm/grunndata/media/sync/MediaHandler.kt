package no.nav.hm.grunndata.media.sync

import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import no.nav.hm.grunndata.media.model.Media
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.MediaStatus
import no.nav.hm.grunndata.media.storage.StorageResponse
import no.nav.hm.grunndata.media.storage.StorageService
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDateTime
import java.util.*


@Singleton
open class MediaHandler(
    private val mediaRepository: MediaRepository,
    private val storageService: StorageService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(MediaHandler::class.java)
    }

    @Transactional
    open suspend fun compareAndPersistMedia(
        mediaInfoList: Set<MediaInfo>,
        mediaInDbList: List<Media>,
        oid: UUID
    ) {
        val newMediaList = mediaInfoList.filter { m -> mediaInDbList.none { m.uri == it.uri } }
        val notInUseList = mediaInDbList.filter { n -> mediaInfoList.none { n.uri == it.uri } }
        val reActiveList = mediaInDbList.filter { inDb -> mediaInfoList.any { inDb.uri == it.uri && it.updated.isAfter(inDb.updated) } }
        LOG.info("Got ${newMediaList.size} new files and ${notInUseList.size} files to be deactivated and ${reActiveList.size} to be reactivated for $oid")
        notInUseList.forEach {
            if (it.status == MediaStatus.ACTIVE)
                mediaRepository.update(it.copy(status = MediaStatus.INACTIVE, updated = LocalDateTime.now()))
        }
        // disable while we migrate to new table
//        reActiveList.forEach {
//            LOG.info("Got new updated media for $oid with uri: ${it.uri} reuploading")
//            uploadToStorage(it)
//            mediaRepository.update(it.copy(status = MediaStatus.ACTIVE, updated = LocalDateTime.now()))
//        }
        newMediaList.forEach {
            LOG.info("Got new media for $oid with uri ${it.uri}")
            if (it.source!=MediaSourceType.EXTERNALURL) {
                // upload and save
                try {
                    mediaRepository.findOneByUri(it.uri)?.let { m ->
                        LOG.debug(
                            """Allowing reuse/shared media, skip upload for this media sourceUri: ${it.sourceUri} uri: ${it.uri}"""
                        )
                        mediaRepository.save(
                            Media(
                                id = UUID.randomUUID(),
                                oid = oid,
                                filename = m.filename,
                                uri = it.uri,
                                size = m.size,
                                type = m.type,
                                sourceUri = m.sourceUri,
                                source = m.source,
                                md5 = m.md5,
                                status = MediaStatus.ACTIVE
                            )
                        )
                    } ?: run {
                        uploadAndCreateMedia(it, oid)
                    }
                } catch (e: Exception) {
                    LOG.error(
                        """Got exception while trying to upload sourceUri: ${it.sourceUri}, uri: ${it.uri} with text: "${it.text}" to cloud""",
                        e
                    )
                    mediaRepository.save(
                        Media(
                            id = UUID.randomUUID(),
                            filename = it.uri,
                            uri = it.uri,
                            oid = oid,
                            size = 0,
                            type = it.type,
                            status = MediaStatus.ERROR,
                            source = it.source,
                            md5 = "",
                            sourceUri = it.sourceUri
                        )
                    )
                }
            }
            else LOG.info("media is external url, skip handling this ${it.uri} ${it.type}")
        }
    }

    // used when we just want to upload, and dont care about database.
    suspend fun uploadSkipDatabaseUpdate(mediaInfos: Set<MediaInfo>) {
       mediaInfos.forEach {
            if (it.source != MediaSourceType.EXTERNALURL) // skip external urls
                uploadToStorage(it)
       }
    }

    private suspend fun uploadToStorage(media: Media): StorageResponse {
        val sourceUri = URI(media.sourceUri)
        val destinationURI = URI(media.uri)
        val contentType = sourceUri.getContentType()
        LOG.info("uploading file to $destinationURI with content type $contentType")
        val upload =
            storageService.uploadStream(
                sourceUri = sourceUri,
                destinationUri = destinationURI,
                contentType = contentType
            )
        return upload
    }

    private suspend fun uploadAndCreateMedia(mediaInfo: MediaInfo,
                                             oid: UUID) {
        val upload = uploadToStorage(mediaInfo)
        mediaRepository.save(
            Media(
                id = UUID.randomUUID(),
                filename = mediaInfo.uri,
                uri = mediaInfo.uri,
                oid = oid,
                size = upload.size,
                type = mediaInfo.type,
                sourceUri = mediaInfo.sourceUri,
                source = mediaInfo.source,
                md5 = upload.md5hash,
                status = MediaStatus.ACTIVE
            )
        )
    }

    private suspend fun uploadToStorage(mediaInfo: MediaInfo): StorageResponse {
        val sourceUri = URI(mediaInfo.sourceUri)
        val destinationURI = URI(mediaInfo.uri)
        val contentType = sourceUri.getContentType()
        LOG.info("uploading file to $destinationURI with content type $contentType")
        val upload =
            storageService.uploadStream(
                sourceUri = sourceUri,
                destinationUri = destinationURI,
                contentType = contentType
            )
        return upload
    }

}

fun URI.getContentType(): String = when (path.lowercase().substringAfterLast(".")) {
    "jpg" -> "image/jpeg"
    "png" -> "image/png"
    "pdf" -> "application/pdf"
    "mp4" -> "video/mp4"
    else -> "application/octet-stream"
}
