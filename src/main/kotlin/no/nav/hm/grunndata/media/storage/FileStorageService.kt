package no.nav.hm.grunndata.media.storage

import io.micronaut.context.annotation.Requires
import io.micronaut.http.multipart.CompletedFileUpload
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI

@Singleton
@Requires(env = ["local"])
class FileStorageService: StorageService {

    companion object {
        private val LOG = LoggerFactory.getLogger(FileStorageService::class.java)
    }

    override fun uploadStream(sourceUri: URI, destinationUri: URI, contentType: String): StorageResponse {
        TODO("Not yet implemented")
    }

    override fun uploadStream(inputStream: InputStream, destinationUri: URI, contentType: String): StorageResponse {
        TODO("Not yet implemented")
    }

    override fun uploadFile(file: CompletedFileUpload, destinationUri: URI): StorageResponse {
        val key = makeKey(destinationUri)
        val destinatioFile = File(key)
        val destinationDirectory = File(destinatioFile.parent)
        if (!destinationDirectory.exists()) destinationDirectory.mkdirs()
        LOG.info("Storing file $key in path ${destinatioFile.parent} in localhost")
        FileOutputStream(destinatioFile).use {
            it.write(file.bytes)
        }
        return StorageResponse(etag = "", key = key, size = file.size, md5hash = "")
    }

    override fun delete(uri: URI): Boolean {
        val key = makeKey(uri)
        LOG.info("Deleting $key from localhost")
        return File(key).delete()
    }

}