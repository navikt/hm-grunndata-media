package no.nav.hm.grunndata.media.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageConfiguration
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageOperations
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.URI


@Singleton
class StorageUpload(private val objectStorage: GoogleCloudStorageOperations,
                    private val storage: Storage,
                    private val config: GoogleCloudStorageConfiguration
) {

    companion object {
        private const val PREFIX = "grunndata/media"
        private val LOG = LoggerFactory.getLogger(StorageUpload::class.java)
    }

    fun uploadStream(uri: URI): StorageResponse {
        val objectName = uri.path.substringAfterLast("/").trim()
        val key = "$PREFIX/$objectName"
        LOG.info("Store $key to gcp")
        val blobId: BlobId = BlobId.of(config.bucket, key)
        val blobInfo = BlobInfo.newBuilder(blobId).build()
        val blob = storage.createFrom(blobInfo, uri.toURL().openStream())
        return StorageResponse(eTag = blob.etag, key = key, size = blob.size,
            md5hash = blob.md5ToHexString)
    }

}
