package no.nav.hm.grunndata.media.sync

import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import no.nav.hm.grunndata.media.model.Media
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.MediaStatus
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
        LOG.info("Got ${newMediaList.size} new files and ${notInUseList.size} files to be deactivated")
        notInUseList.forEach {
            if (it.status == MediaStatus.ACTIVE)
                mediaRepository.update(it.copy(status = MediaStatus.INACTIVE, updated = LocalDateTime.now()))
        }
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

    private suspend fun uploadAndCreateMedia(mediaInfo: MediaInfo,
                                             oid: UUID) {
        val sourceUri = URI(mediaInfo.sourceUri)
        val destinationURI = URI(mediaInfo.uri)
        val contentType = sourceUri.getContentType()
        val upload =
            storageService.uploadStream(
                sourceUri = sourceUri,
                destinationUri = destinationURI,
                contentType = contentType
            )
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
                status = MediaStatus.ACTIVE,
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
