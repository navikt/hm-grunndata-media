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
import java.io.InputStream
import java.net.URI


@Singleton
class GCStorageStorageService(
    private val storage: Storage,
    private val config: GoogleCloudStorageConfiguration,
    private val mediaConfig: MediaStorageConfig,
    private val gcsOperations: GoogleCloudStorageOperations,
) : StorageService {

    companion object {
        private const val PREFIX = "teamdigihot/grunndata/media/v1"
        private val LOG = LoggerFactory.getLogger(GCStorageStorageService::class.java)
    }

    init {
        LOG.info(
            """GCS Storage enabled is ${mediaConfig.enabled}, using bucket ${config.bucket}, storing to ${PREFIX}"""
        )
    }

    override fun uploadStream(sourceUri: URI, destinationUri: URI, contentType: String): StorageResponse {
        val key = makeKey(destinationUri)
        LOG.info("Uploading ${key} from sourceUri $sourceUri")
        val blobId: BlobId = BlobId.of(config.bucket, key)
        val blobInfo = BlobInfo.newBuilder(blobId).apply {
            setContentType(contentType)
        }.build()
        return sourceUri.toURL().openStream().use {
            val blob = storage.createFrom(blobInfo, it)
            StorageResponse(
                etag = blob.etag, key = key, size = blob.size,
                md5hash = blob.md5ToHexString
            )
        }

    }

    override fun uploadStream(source: InputStream, destinationUri: URI, contentType: String): StorageResponse {
        val key = makeKey(destinationUri)
        LOG.info("Uploading ${key} from inputstream")
        val blobId: BlobId = BlobId.of(config.bucket, key)
        val blobInfo = BlobInfo.newBuilder(blobId).apply {
            setContentType(contentType)
        }.build()
        return source.use {
            val blob = storage.createFrom(blobInfo, it)
            StorageResponse(
                etag = blob.etag, key = key, size = blob.size,
                md5hash = blob.md5ToHexString
            )
        }
    }

    override fun uploadFile(file: CompletedFileUpload, destinationUri: URI): StorageResponse {
        val key = makeKey(destinationUri)
        val response = gcsOperations.upload(UploadRequest.fromCompletedFileUpload(file, key)).nativeResponse
        return StorageResponse(etag = response.etag, key = key, size = response.size, md5hash = response.md5ToHexString)
    }

    private fun makeKey(destinationUri: URI, sizeVersion: String? = null): String {
        val objectName = destinationUri.path
        return if (sizeVersion != null) "$PREFIX/$sizeVersion/$objectName" else "$PREFIX/$objectName"
    }


    override fun delete(uri: URI): Boolean {
        val key = makeKey(uri)
        val smallKey = makeKey(uri, "small")
        LOG.info("Deleting $key and $smallKey from gcp bucket")
        val smallBlobId: BlobId = BlobId.of(config.bucket, smallKey)
        storage.delete(smallBlobId)
        val blobId: BlobId = BlobId.of(config.bucket, key)
        return storage.delete(blobId)
    }

}
