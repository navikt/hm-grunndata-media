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
            val mediaList = mediaRepository.findByStatusInListAndUpdatedBefore(listOf(MediaStatus.INACTIVE, MediaStatus.DELETED, MediaStatus.ERROR), olderThan)
            if (mediaList.size > 5000) {
                LOG.error("Too many files on delete list ${mediaList.size}, please check if it is correct")
                //return@runBlocking
            }
            LOG.info("Found ${mediaList.size} to be deleted")
            mediaList.forEach { media ->
                mediaRepository.findOneByUriAndStatus(media.uri, MediaStatus.ACTIVE)?.let {
                    LOG.debug("used by at another object, skip deleting media file")
                } ?: run {
                    LOG.info("Deleting file from storage: ${media.uri}")
                    storageService.delete(URI(media.uri))
                }
                mediaRepository.delete(media)
            }
        }
    }

}
