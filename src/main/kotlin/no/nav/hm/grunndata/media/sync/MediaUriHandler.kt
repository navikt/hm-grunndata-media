package no.nav.hm.grunndata.media.sync

import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import no.nav.hm.grunndata.media.model.MediaStatus
import no.nav.hm.grunndata.media.model.MediaUri
import no.nav.hm.grunndata.media.model.MediaUriRepository
import no.nav.hm.grunndata.media.model.ObjectType
import no.nav.hm.grunndata.media.storage.StorageResponse
import no.nav.hm.grunndata.media.storage.StorageService
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDateTime
import java.util.*

@Singleton
open class MediaUriHandler(private val mediaUriRepository: MediaUriRepository,
                           private val storageService: StorageService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(MediaUriHandler::class.java)
    }

    @Transactional
    open suspend fun compareAndPersistMedia(
        mediaInfoList: Set<MediaInfo>,
        mediaInDbList: List<MediaUri>,
        oid: UUID,
        objectType: ObjectType
    ) {
        val newMediaList = mediaInfoList.filter { m -> mediaInDbList.none { m.uri == it.uri } }
        val notInUseList = mediaInDbList.filter { n -> mediaInfoList.none { n.uri == it.uri } }
        val reActiveList = mediaInDbList.filter { inDb -> mediaInfoList.any { inDb.uri == it.uri && it.updated.isAfter(inDb.updated) } }
        LOG.info("Got ${newMediaList.size} new files and ${notInUseList.size} files to be deactivated and ${reActiveList.size} to be reactivated for $oid")
        notInUseList.forEach {
            if (it.status == MediaStatus.ACTIVE)
                mediaUriRepository.update(it.copy(status = MediaStatus.INACTIVE, updated = LocalDateTime.now()))
        }
        reActiveList.forEach {
            LOG.info("Got new updated media for $oid with uri: ${it.uri} reuploading")
            if (it.source==MediaSourceType.HMDB) uploadToStorage(it.sourceUri, it.uri) // reload only if HMDB
            mediaUriRepository.update(it.copy(status = MediaStatus.ACTIVE, updated = LocalDateTime.now()))
        }
        newMediaList.forEach {
            LOG.info("Got new media for $oid with uri ${it.uri}")
            if (it.source!= MediaSourceType.EXTERNALURL) {
                // upload and save
                try {
                    uploadAndCreateMedia(it, oid, objectType)
                } catch (e: Exception) {
                    LOG.error(
                        """Got exception while trying to upload sourceUri: ${it.sourceUri}, uri: ${it.uri} with text: "${it.text}" to cloud""",
                        e
                    )
                    mediaUriRepository.save(
                        MediaUri(
                            filename = it.uri,
                            uri = it.uri,
                            oid = oid,
                            size = 0,
                            type = it.type,
                            status = MediaStatus.ERROR,
                            source = it.source,
                            objectType = objectType,
                            md5 = "",
                            sourceUri = it.sourceUri
                        )
                    )
                }
            }
            else LOG.info("media is external url, skip handling this ${it.uri} ${it.type}")
        }
    }


    private suspend fun uploadToStorage(sourceUri: String, uri:String): StorageResponse {
        val sourceUri = URI(sourceUri)
        val destinationURI = URI(uri)
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
                                             oid: UUID, objectType: ObjectType) {
        val upload = uploadToStorage(mediaInfo.sourceUri, mediaInfo.uri)
        mediaUriRepository.save(
            MediaUri(
                filename = mediaInfo.uri,
                uri = mediaInfo.uri,
                oid = oid,
                size = upload.size,
                type = mediaInfo.type,
                sourceUri = mediaInfo.sourceUri,
                source = mediaInfo.source,
                md5 = upload.md5hash,
                objectType = objectType,
                status = MediaStatus.ACTIVE
            )
        )
    }
}

fun URI.getContentType(): String = when (path.lowercase().substringAfterLast(".")) {
    "jpg" -> "image/jpeg"
    "png" -> "image/png"
    "pdf" -> "application/pdf"
    "mp4" -> "video/mp4"
    else -> "application/octet-stream"
}
