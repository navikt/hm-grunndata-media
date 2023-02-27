package no.nav.hm.grunndata.media.storage


import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.net.URI

@MicronautTest
class MediaUploadTest(
    private val gcstorageUpload: GCStorageStorageUpload,
    private val objectMapper: ObjectMapper
) {

    @Test
    fun testUploadDownload() {
        val resource = MediaUploadTest::class.java.classLoader.getResource("54216.jpg")
        val uri = URI("https://www.hjelpemiddeldatabasen.no/blobs/orig/54216.jpg")
        val response = gcstorageUpload.uploadStream(uri)
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response))
        println(gcstorageUpload.delete(uri))
    }
}
