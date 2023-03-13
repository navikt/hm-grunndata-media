package no.nav.hm.grunndata.media.storage

import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.MediaStatus
import org.slf4j.LoggerFactory
import java.net.URI
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
            LOG.info("found ${mediaList.size} to be deleted")
            storageService.deleteList(mediaList.map { URI(it.mediaId.uri) })
            mediaRepository.deleteAll(mediaList)
        }
    }
}
