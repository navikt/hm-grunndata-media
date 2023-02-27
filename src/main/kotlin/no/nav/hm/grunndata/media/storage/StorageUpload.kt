package no.nav.hm.grunndata.media.storage

import java.net.URI

interface StorageUpload {

    fun uploadStream(uri: URI): StorageResponse
}
