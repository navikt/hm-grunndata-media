package no.nav.hm.grunndata.media.storage

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
class DeleteOldMediaScheduler(
    private val deleteOldMedia: DeleteOldMedia
) {
    
    @Scheduled(cron = "0 45 0 * * *")
    fun deleteOldFiles() {
        deleteOldMedia.deleteOldFiles()
    }
}
