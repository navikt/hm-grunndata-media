package no.nav.hm.grunndata.media.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import io.micronaut.context.annotation.Value
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageConfiguration
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.URI


@Singleton
class GCStorageStorageUpload(
    private val storage: Storage,
    private val config: GoogleCloudStorageConfiguration,
    @Value("\${media.storage.enabled}:false") private val enabled: Boolean = false
) : StorageUpload {

    companion object {
        private const val PREFIX = "grunndata/media"
        private val LOG = LoggerFactory.getLogger(GCStorageStorageUpload::class.java)
    }

    override fun uploadStream(uri: URI): StorageResponse {
        return if (enabled) {
            val objectName = uri.path.substringAfterLast("/").trim()
            val key = "$PREFIX/$objectName"
            LOG.info("Store $key to gcp")
            val blobId: BlobId = BlobId.of(config.bucket, key)
            val blobInfo = BlobInfo.newBuilder(blobId).build()
            val blob = storage.createFrom(blobInfo, uri.toURL().openStream())
            StorageResponse(
                eTag = blob.etag, key = key, size = blob.size,
                md5hash = blob.md5ToHexString
            )
        } else StorageResponse(
            eTag = "notstored", key = "notstored", size = 0,
            md5hash = "notstored"
        )
    }

}
