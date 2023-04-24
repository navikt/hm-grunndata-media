package no.nav.hm.grunndata.media.sync

import jakarta.inject.Singleton
import no.nav.hm.grunndata.media.imageio.ImageHandler
import no.nav.hm.grunndata.media.imageio.ImageHandler.Companion.SMALL
import no.nav.hm.grunndata.media.model.Media
import no.nav.hm.grunndata.media.model.MediaId
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.MediaStatus
import no.nav.hm.grunndata.media.storage.StorageService
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.MediaType
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional


@Singleton
open class MediaHandler(
    private val mediaRepository: MediaRepository,
    private val storageService: StorageService,
    private val imageHandler: ImageHandler
) {


    companion object {
        private val LOG = LoggerFactory.getLogger(MediaHandler::class.java)
    }

    @Transactional
    open suspend fun compareAndPersistMedia(
        mediaInfoList: List<MediaInfo>,
        mediaInDbList: List<Media>,
        oid: UUID
    ) {
        val newMediaList = mediaInfoList.filter { m -> mediaInDbList.none { m.uri == it.mediaId.uri } }
        val notInUseList = mediaInDbList.filter { n -> mediaInfoList.none { n.mediaId.uri == it.uri } }
        LOG.info("Got ${newMediaList.size} new files and ${notInUseList.size} files to be deactivated")
        notInUseList.forEach {
            if (it.status == MediaStatus.ACTIVE)
                mediaRepository.update(it.copy(status = MediaStatus.INACTIVE, updated = LocalDateTime.now()))
        }
        newMediaList.forEach {
            // upload and save
            try {
                mediaRepository.findOneByMediaIdUri(it.uri)?.let { m ->
                    LOG.debug(
                        """Allowing reuse media, skip upload for this media uri: ${m.mediaId.uri}"""
                    )
                    mediaRepository.save(
                        Media(
                            mediaId = MediaId(oid = oid, uri = it.uri),
                            size = m.size, type = m.type, sourceUri = m.sourceUri, source = m.source,
                            md5 = m.md5, status = m.status
                        )
                    )
                } ?: run {
                    uploadAndCreateMedia(it, oid)
                }
            } catch (e: Exception) {
                LOG.error("""Got exception while trying to upload ${it.uri} with text "${it.text}" to cloud""", e)
                mediaRepository.save(
                    Media(
                        mediaId = MediaId(uri = it.uri, oid = oid),
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
        if (MediaType.IMAGE == mediaInfo.type && upload.size > 0) {
            val smallUri = "small/${mediaInfo.uri}"
            val resp = imageHandler.createImageVersionInputStream(sourceUri, SMALL)?.let {
                storageService.uploadStream(it, URI(smallUri), contentType)
            }
            LOG.info("created small version: $smallUri with size: ${resp?.size}")
        }

        mediaRepository.save(
            Media(
                mediaId = MediaId(uri = mediaInfo.uri, oid = oid),
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
    else -> "application/octet-stream"
}
