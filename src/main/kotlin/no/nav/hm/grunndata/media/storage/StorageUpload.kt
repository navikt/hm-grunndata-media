package no.nav.hm.grunndata.media.storage

import java.net.URI

interface StorageUpload {
    fun uploadStream(sourceUri: URI, destinationUri: URI): StorageResponse

    fun delete(uri: URI): Boolean
}
