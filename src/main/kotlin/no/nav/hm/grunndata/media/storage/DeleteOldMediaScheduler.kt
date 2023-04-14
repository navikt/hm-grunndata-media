package no.nav.hm.grunndata.media.storage

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.nav.hm.grunndata.media.LeaderElection

@Singleton
@Requires(property = "schedulers.enabled", value = "true")
class DeleteOldMediaScheduler(
    private val deleteOldMedia: DeleteOldMedia, private val leaderElection: LeaderElection
) {

    @Scheduled(fixedDelay = "5m")
    fun deleteOldFiles() {
        if (leaderElection.isLeader()) {
            deleteOldMedia.deleteOldFiles()
        }
    }

    @Scheduled(fixedDelay = "30m")
    fun deleteErrorFiles() {
        if (leaderElection.isLeader()) {
            deleteErrorFiles()
        }
    }
}
