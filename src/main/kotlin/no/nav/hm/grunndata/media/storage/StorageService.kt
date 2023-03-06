package no.nav.hm.grunndata.media.storage

import java.net.URI

interface StorageService {
    fun uploadStream(sourceUri: URI, destinationUri: URI): StorageResponse

    fun delete(uri: URI): Boolean

    fun deleteList(uriList: List<URI>): Boolean
}
