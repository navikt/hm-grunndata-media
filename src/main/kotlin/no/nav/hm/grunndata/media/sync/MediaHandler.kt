package no.nav.hm.grunndata.media.sync

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.media.model.Media
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.MediaStatus
import no.nav.hm.grunndata.media.storage.StorageService
import no.nav.hm.grunndata.rapid.dto.MediaDTO
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
        dtoMediaList: List<MediaDTO>,
        mediaStateList: List<Media>,
        oid: UUID,
        ownerDto: Any,
    ) {
        val newMediaList = dtoMediaList.filter { m -> mediaStateList.none { m.uri == it.uri } }
        val notInUseList = mediaStateList.filter { n -> dtoMediaList.none { n.uri == it.uri } }
        notInUseList.forEach {
            mediaRepository.update(it.copy(status = MediaStatus.INACTIVE, updated = LocalDateTime.now()))
        }
        newMediaList.forEach {
            // upload and save
            try {
                val upload = storageService.uploadStream(sourceUri = URI(it.sourceUri), destinationUri = URI(it.uri))
                mediaRepository.save(
                    Media(
                        uri = it.uri, oid = oid, size = upload.size, type = it.type, sourceUri = it.sourceUri,
                        priority = it.priority, source = it.source, text = it.text, md5 = upload.md5hash
                    )
                )
            } catch (e: Exception) {
                LOG.error("Got exception while trying to upload ${it.uri} to cloud", e)
                mediaRepository.save(
                    Media(
                        uri = it.uri, oid = oid, size = 0, type = it.type, status = MediaStatus.ERROR,
                        priority = it.priority, source = it.source, text = it.text, md5 = "", sourceUri = it.sourceUri
                    )
                )
            }
        }
    }

}
