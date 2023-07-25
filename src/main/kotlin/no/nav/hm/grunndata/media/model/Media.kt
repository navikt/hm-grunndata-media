package no.nav.hm.grunndata.media.model


import io.micronaut.data.annotation.Embeddable
import io.micronaut.data.annotation.EmbeddedId
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import jakarta.persistence.Column


import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import java.time.LocalDateTime
import java.util.*

@MappedEntity("media_v1")
data class Media(
    @field:Id
    val id: UUID,
    val oid: UUID,
    val uri: String,
    val sourceUri: String,
    val type: MediaType = MediaType.IMAGE,
    val size: Long,
    val md5: String,
    val status: MediaStatus = MediaStatus.ACTIVE,
    val source: MediaSourceType = MediaSourceType.HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
)


enum class MediaStatus {
    ACTIVE, INACTIVE, ERROR
}


fun Media.toDTO(): MediaDTO = MediaDTO(
    id = id,
    oid = oid,
    uri = uri,
    sourceUri = sourceUri,
    source = source,
    size = size,
    md5 = md5,
    type = type,
    status = status,
    created = created,
    updated = updated,
)

data class MediaDTO(
    val id: UUID,
    val oid: UUID,
    val uri: String,
    val sourceUri: String,
    val type: MediaType = MediaType.IMAGE,
    val size: Long,
    val md5: String,
    val status: MediaStatus = MediaStatus.ACTIVE,
    val source: MediaSourceType = MediaSourceType.HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
)
