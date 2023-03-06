package no.nav.hm.grunndata.media.storage


import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import java.net.URI

@MicronautTest
class MediaUploadTest(
    private val gcstorageUpload: GCStorageStorageService,
    private val objectMapper: ObjectMapper
) {

    //@Test //use for testing gcp buckets
    fun testUploadDownload() {
        val resource = MediaUploadTest::class.java.classLoader.getResource("54216.jpg")
        val sourceUri = URI("https://www.hjelpemiddeldatabasen.no/blobs/orig/54216.jpg")
        val destinationUri = URI("123_54216.jpg")
        val response = gcstorageUpload.uploadStream(sourceUri, destinationUri)
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response))
    }
}
