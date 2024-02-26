package no.nav.hm.grunndata.media.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import io.micronaut.context.annotation.Requires
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageConfiguration
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageOperations
import io.micronaut.objectstorage.request.UploadRequest
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.URI


@Singleton
@Requires(notEnv = ["local"])
class GCStorageStorageService(
    private val storage: Storage,
    private val config: GoogleCloudStorageConfiguration,
    private val mediaConfig: MediaStorageConfig,
    private val gcsOperations: GoogleCloudStorageOperations,
) : StorageService {

    companion object {
        private val LOG = LoggerFactory.getLogger(GCStorageStorageService::class.java)
    }

    init {
        LOG.info(
            """GCS Storage enabled is ${mediaConfig.enabled}, using bucket ${config.bucket}, storing to ${STORAGE_PREFIX}"""
        )
    }

    override suspend fun uploadStream(sourceUri: URI, destinationUri: URI, contentType: String): StorageResponse {
        val key = makeKey(destinationUri)
        LOG.info("Uploading ${key} from sourceUri $sourceUri")
        val blobId: BlobId = BlobId.of(config.bucket, key)
        val blobInfo = BlobInfo.newBuilder(blobId).apply {
            setContentType(contentType)
        }.build()
        return withContext(Dispatchers.IO) {
            sourceUri.toURL().openStream()
                .use {
                    val blob = storage.createFrom(blobInfo, it)
                    StorageResponse(
                        etag = blob.etag, key = key, size = blob.size,
                        md5hash = blob.md5ToHexString
                    )
                }
        }

    }

    override suspend fun uploadStream(
        inputStream: InputStream,
        destinationUri: URI,
        contentType: String
    ): StorageResponse {
        val key = makeKey(destinationUri)
        LOG.info("Uploading ${key} from inputstream")
        val blobId: BlobId = BlobId.of(config.bucket, key)
        val blobInfo = BlobInfo.newBuilder(blobId).apply {
            setContentType(contentType)
        }.build()
        return withContext(Dispatchers.IO) {
            inputStream.use {
                val blob = storage.createFrom(blobInfo, it)
                StorageResponse(
                    etag = blob.etag, key = key, size = blob.size,
                    md5hash = blob.md5ToHexString
                )
            }
        }
    }

    override suspend fun uploadFile(file: CompletedFileUpload, destinationUri: URI): StorageResponse {
        val key = makeKey(destinationUri)
        return withContext(Dispatchers.Default) {
            val response = gcsOperations.upload(UploadRequest.fromCompletedFileUpload(file, key)).nativeResponse
            StorageResponse(
                etag = response.etag,
                key = key,
                size = response.size,
                md5hash = response.md5ToHexString
            )
        }
    }


    override suspend fun delete(uri: URI): Boolean {
        val key = makeKey(uri)
        LOG.info("Deleting $key from gcp bucket")
        val blobId: BlobId = BlobId.of(config.bucket, key)
        return withContext(Dispatchers.IO) { storage.delete(blobId) }
    }

}
