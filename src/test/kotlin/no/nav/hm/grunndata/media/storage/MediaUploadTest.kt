package no.nav.hm.grunndata.media.storage


import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.media.sync.getContentType
import org.junit.jupiter.api.Test
import java.net.URI

//@MicronautTest
class MediaUploadTest(
    private val gcstorageUpload: GCStorageStorageService,
    private val objectMapper: ObjectMapper
) {
    //@Test //use for testing gcp buckets
    fun testUploadDownload() {
        val resource = MediaUploadTest::class.java.classLoader.getResource("66131.jpg")
        val sourceUri = URI("https://www.hjelpemiddeldatabasen.no/blobs/orig/66131.jpg")
        val destinationUri = URI("123_66131.jpg")
        runBlocking {
            val response = gcstorageUpload.uploadStream(sourceUri, destinationUri, sourceUri.getContentType())
            println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response))
            gcstorageUpload.delete(destinationUri)
        }


    }
}
