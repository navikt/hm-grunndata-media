package no.nav.hm.grunndata.media.storage

import io.micronaut.http.multipart.CompletedFileUpload
import java.io.InputStream
import java.net.URI

interface StorageService {


    fun uploadStream(sourceUri: URI, destinationUri: URI, contentType: String): StorageResponse

    fun uploadStream(inputStream: InputStream, destinationUri: URI, contentType: String): StorageResponse

    fun uploadFile(file: CompletedFileUpload, destinationUri: URI): StorageResponse

    fun delete(uri: URI): Boolean


    
}

fun makeKey(destinationUri: URI): String {
    val objectName = destinationUri.path
    return "${STORAGE_PREFIX}/$objectName"
}
const val STORAGE_PREFIX = "teamdigihot/grunndata/media/v1"