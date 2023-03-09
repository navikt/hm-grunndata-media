package no.nav.hm.grunndata.media.model

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import no.nav.hm.grunndata.rapid.dto.MediaDTO
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType

import java.time.LocalDateTime
import java.util.*

@MappedEntity("media_v1")
data class Media(
    @field:Id
    val uri: String,
    val oid: UUID,
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
    uri = uri,
    sourceUri = sourceUri,
    source = source,
    type = type,
)


