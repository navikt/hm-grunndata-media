package no.nav.hm.grunndata.media.sync

import jakarta.inject.Singleton
import no.nav.hm.grunndata.media.model.Media
import no.nav.hm.grunndata.media.model.MediaId
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.MediaStatus
import no.nav.hm.grunndata.media.storage.StorageService
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional

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
        mediaInfoList: List<MediaInfo>,
        mediaInDbList: List<Media>,
        oid: UUID
    ) {
        val newMediaList = mediaInfoList.filter { m -> mediaInDbList.none { m.uri == it.mediaId.uri } }
        val notInUseList = mediaInDbList.filter { n -> mediaInfoList.none { n.mediaId.uri == it.uri } }
        LOG.info("Got ${newMediaList.size} new files and ${notInUseList.size} files to be deactivated")
        notInUseList.forEach {
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
                            size = m.size, type = m.type, sourceUri = m.sourceUri, source = m.source, md5 = m.md5
                        )
                    )
                } ?: run {
                    val upload =
                        storageService.uploadStream(
                            sourceUri = URI(it.sourceUri),
                            destinationUri = URI(it.uri),
                            contentType = URI(it.sourceUri).getContentType()
                        )
                    mediaRepository.save(
                        Media(
                            mediaId = MediaId(uri = it.uri, oid = oid),
                            size = upload.size,
                            type = it.type,
                            sourceUri = it.sourceUri,
                            source = it.source,
                            md5 = upload.md5hash
                        )
                    )
                }
            } catch (e: Exception) {
                LOG.error("Got exception while trying to upload ${it.uri} to cloud", e)
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

}

fun URI.getContentType(): String = when (path.lowercase().substringAfterLast(".")) {
    "jpg" -> "image/jpeg"
    "png" -> "image/png"
    "pdf" -> "application/pdf"
    else -> "application/octet-stream"
}
