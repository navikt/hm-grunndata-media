package no.nav.hm.grunndata.media.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageConfiguration
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageOperations
import io.micronaut.objectstorage.request.UploadRequest
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.URI


@Singleton
class GCStorageStorageService(
    private val storage: Storage,
    private val config: GoogleCloudStorageConfiguration,
    private val mediaConfig: MediaStorageConfig,
    private val gcsOperations: GoogleCloudStorageOperations,
) : StorageService {

    companion object {
        private const val PREFIX = "grunndata/media"
        private val LOG = LoggerFactory.getLogger(GCStorageStorageService::class.java)
    }

    init {
        LOG.info("GCS Storage enabled is ${mediaConfig.enabled} and using bucket ${config.bucket}")
    }

    override fun uploadStream(sourceUri: URI, destinationUri: URI): StorageResponse {
        return if (mediaConfig.enabled) {
            val key = makeKey(destinationUri)
            LOG.info("Uploading ${key}")
            val blobId: BlobId = BlobId.of(config.bucket, key)
            val blobInfo = BlobInfo.newBuilder(blobId).build()
            sourceUri.toURL().openStream().use {
                val blob = storage.createFrom(blobInfo, it)
                StorageResponse(
                    etag = blob.etag, key = key, size = blob.size,
                    md5hash = blob.md5ToHexString
                )
            }
        } else StorageResponse(
            etag = "notstored", key = "notstored", size = 0,
            md5hash = "notstored"
        )
    }

    override fun uploadFile(file: CompletedFileUpload, destinationUri: URI): StorageResponse {
        val key = makeKey(destinationUri)
        val response = gcsOperations.upload(UploadRequest.fromCompletedFileUpload(file, key)).nativeResponse
        return StorageResponse(etag = response.etag, key = key, size = response.size, md5hash = response.md5ToHexString)
    }

    private fun makeKey(destinationUri: URI): String {
        val objectName = destinationUri.path.substringAfterLast("/").trim()
        val key = "$PREFIX/$objectName"
        LOG.debug("Got $key")
        return key
    }


    override fun delete(uri: URI): Boolean {
        val key = makeKey(uri)
        LOG.info("Deleting $key from gcp bucket")
        val blobId: BlobId = BlobId.of(config.bucket, key)
        return storage.delete(blobId)
    }

    override fun deleteList(uriList: List<URI>): Boolean {
        val batchId = uriList.map {
            BlobId.of(config.bucket, makeKey(it))
        }
        return storage.delete(batchId)[0]
    }

}
