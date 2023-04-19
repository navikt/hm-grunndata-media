package no.nav.hm.grunndata.media

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import no.nav.hm.grunndata.media.storage.StorageService
import org.slf4j.LoggerFactory
import java.net.URI

@Controller("/internal")
class AliveController(private val storageService: StorageService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AliveController::class.java)
    }

    @Get("/isAlive")
    fun alive() = "ALIVE"

    @Get("/isReady")
    fun ready() = "OK"

    @Post("/check/bucket")
    fun checkBucketUpload(): String {
        val sourceUri = URI("https://www.hjelpemiddeldatabasen.no/blobs/orig/64661.jpg")
        val destinationUri = URI("64661-test.jpg")
        LOG.info("Check upload bucket, uploading..")
        storageService.uploadStream(sourceUri, destinationUri, "image/jpeg")
        LOG.info("upload works, deleting")
        storageService.delete(destinationUri)
        return "SUCCESS"
    }

}
