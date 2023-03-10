package no.nav.hm.grunndata.media.model


import io.micronaut.data.annotation.Embeddable
import io.micronaut.data.annotation.EmbeddedId
import io.micronaut.data.annotation.MappedEntity
import jakarta.persistence.Column

import no.nav.hm.grunndata.rapid.dto.MediaDTO
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import java.time.LocalDateTime
import java.util.*

@MappedEntity("media_v1")
data class Media(
    @field:EmbeddedId
    val mediaId: MediaId,
    val sourceUri: String,
    val type: MediaType = MediaType.IMAGE,
    val size: Long,
    val md5: String,
    val status: MediaStatus = MediaStatus.ACTIVE,
    val source: MediaSourceType = MediaSourceType.HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
)

@Embeddable
data class MediaId(
    @field:Column(name = "oid")
    val oid: UUID,
    @field:Column(name = "uri")
    val uri: String
)

enum class MediaStatus {
    ACTIVE, INACTIVE, ERROR
}

fun Media.toDTO(): MediaDTO = MediaDTO(
    oid = mediaId.oid,
    uri = mediaId.uri,
    sourceUri = sourceUri,
    source = source,
    type = type,
)


