package no.nav.hm.grunndata.media.model

import io.micronaut.data.annotation.MappedEntity
import jakarta.persistence.Id
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import java.time.LocalDateTime
import java.util.*


@MappedEntity("media_uri_v1")
data class MediaUri(
    @field:Id
    val uri: String,
    val sourceUri: String,
    val oid: UUID,
    val objectType: ObjectType,
    val md5: String,
    val size: Long,
    val filename: String,
    val type: MediaType,
    val status: MediaStatus = MediaStatus.ACTIVE,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val source: MediaSourceType = MediaSourceType.HMDB,
)

enum class ObjectType {
    SERIES,
    PRODUCT,
    AGREEMENT,
    UNKNOWN
}
