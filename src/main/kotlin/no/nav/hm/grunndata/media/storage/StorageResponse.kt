package no.nav.hm.grunndata.media.storage

data class StorageResponse(val key: String, val size: Long, val md5hash: String, val etag: String)
