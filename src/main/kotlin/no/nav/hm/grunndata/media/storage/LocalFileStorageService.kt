package no.nav.hm.grunndata.media.storage

import io.micronaut.context.annotation.Requires
import io.micronaut.http.multipart.CompletedFileUpload
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.security.MessageDigest
import java.util.*

@Singleton
@Requires(env = ["local"])
class FileStorageService: StorageService {

    companion object {
        private val LOG = LoggerFactory.getLogger(FileStorageService::class.java)
        const val LOCALPATH = "/tmp"
    }

    override fun uploadStream(sourceUri: URI, destinationUri: URI, contentType: String): StorageResponse {
        TODO("Not yet implemented")
    }

    override fun uploadStream(inputStream: InputStream, destinationUri: URI, contentType: String): StorageResponse {
        TODO("Not yet implemented")
    }

    override fun uploadFile(file: CompletedFileUpload, destinationUri: URI): StorageResponse {
        val key = makeKey(destinationUri)
        val destinatioFile = File("$LOCALPATH/$key")
        val destinationDirectory = File(destinatioFile.parent)
        if (!destinationDirectory.exists()) destinationDirectory.mkdirs()
        LOG.info("Storing file $key in path ${destinatioFile.parent} in localhost")
        FileOutputStream(destinatioFile).use {
            it.write(file.bytes)
        }
        val md5 = file.md5Hex()
        return StorageResponse(etag = md5, key = key, size = file.size, md5hash = md5)
    }

    override fun delete(uri: URI): Boolean {
        val key = makeKey(uri)
        LOG.info("Deleting $key from localhost")
        return File("$LOCALPATH/$key").delete()
    }
}

fun CompletedFileUpload.md5Hex(): String  {
    val instance = MessageDigest.getInstance("MD5")!!
    val digest  = instance.digest(this.bytes)
    val hex = HexFormat.of().formatHex(digest)
    instance.reset()
    return hex
}



