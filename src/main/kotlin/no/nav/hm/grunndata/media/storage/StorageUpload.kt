package no.nav.hm.grunndata.media.storage

import com.google.cloud.storage.Blob
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageOperations
import io.micronaut.objectstorage.request.UploadRequest
import io.micronaut.objectstorage.response.UploadResponse
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.Path

@Singleton
class StorageUpload(private val objectStorage: GoogleCloudStorageOperations) {

    companion object {
        private const val PREFIX = "grunndata/media"
        private val LOG = LoggerFactory.getLogger(StorageUpload::class.java)
    }

    fun upload(uri: URI): StorageResponse {
        LOG.info("Store $uri to gcp")
        val objectStorageUpload: UploadRequest = UploadRequest.fromPath(Path.of(uri), PREFIX)
        val response: UploadResponse<Blob> = objectStorage.upload(objectStorageUpload)
        return StorageResponse(eTag = response.eTag, key = response.key, size = response.nativeResponse.size,
            md5hash = response.nativeResponse.md5ToHexString)
    }

}
