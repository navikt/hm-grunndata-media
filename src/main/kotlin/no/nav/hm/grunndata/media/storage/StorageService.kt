package no.nav.hm.grunndata.media.storage

import io.micronaut.http.multipart.CompletedFileUpload
import java.io.InputStream
import java.net.URI

interface StorageService {


    suspend fun uploadStream(sourceUri: URI, destinationUri: URI, contentType: String): StorageResponse

    suspend fun uploadStream(inputStream: InputStream, destinationUri: URI, contentType: String): StorageResponse

    suspend fun uploadFile(file: CompletedFileUpload, destinationUri: URI): StorageResponse

    suspend fun delete(uri: URI): Boolean

}

fun makeKey(destinationUri: URI): String {
    val objectName = destinationUri.path
    return "${STORAGE_PREFIX}/$objectName"
}
const val STORAGE_PREFIX = "teamdigihot/grunndata/media/v1"