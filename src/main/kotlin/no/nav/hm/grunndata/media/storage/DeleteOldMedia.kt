package no.nav.hm.grunndata.media.storage

import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.MediaStatus
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime

@Singleton
class DeleteOldMedia(
    private val mediaRepository: MediaRepository,
    private val storageService: StorageService,
    private val mediaStorageConfig: MediaStorageConfig
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(DeleteOldMedia::class.java)
    }

    fun deleteOldFiles() {
        val olderThan = LocalDateTime.now().minus(mediaStorageConfig.retention)
        LOG.info("Deleting files that is older than $olderThan and status: ${MediaStatus.INACTIVE}")
        runBlocking {
            val mediaList = mediaRepository.findByStatusAndUpdatedBefore(MediaStatus.INACTIVE, olderThan)
            if (mediaList.size > 1000) {
                LOG.error("Too many files on delete list ${mediaList.size}, please check if it is correct")
                //return@runBlocking
            }
            mediaList.forEach { media ->
                mediaRepository.findOneByMediaIdUriAndStatus(media.mediaId.uri, MediaStatus.ACTIVE)?.let {
                    LOG.debug("used by at another object, skip deleting media file")
                } ?: run {
                    LOG.info("Deleting file from storage: ${media.mediaId.uri}")
                    storageService.delete(URI(media.mediaId.uri))
                }
                mediaRepository.delete(media)
            }
        }
    }

    fun deleteErrorFiles() {
        val olderThan = LocalDateTime.now().minus(Duration.ofDays(10))
        LOG.info("Deleting files that is older than $olderThan and status: ${MediaStatus.ERROR}")
        runBlocking {
            val mediaList = mediaRepository.findByStatusAndUpdatedBefore(MediaStatus.ERROR, olderThan)
            if (mediaList.size > 1000) {
                LOG.error("Too many files on delete list ${mediaList.size}, please check if it is correct")
                return@runBlocking
            }
            mediaList.forEach { media ->
                mediaRepository.findOneByMediaIdUriAndStatus(media.mediaId.uri, MediaStatus.ACTIVE)?.let {
                    LOG.info("used by at another object ${it.mediaId.oid}, skip deleting media file from cloud storage")
                } ?: run {
                    LOG.info("Deleting file from storage: ${media.mediaId.uri}")
                    storageService.delete(URI(media.mediaId.uri))
                }
                mediaRepository.delete(media)
            }
        }
    }
}
