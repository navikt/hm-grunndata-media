package no.nav.hm.grunndata.media.storage

import io.micronaut.http.multipart.CompletedFileUpload
import java.net.URI

interface StorageService {
    fun uploadStream(sourceUri: URI, destinationUri: URI, contentType: String): StorageResponse

    fun uploadFile(file: CompletedFileUpload, destinationUri: URI): StorageResponse

    fun delete(uri: URI): Boolean

    fun deleteList(uriList: List<URI>): Boolean
}
