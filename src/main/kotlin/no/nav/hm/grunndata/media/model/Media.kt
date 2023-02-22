package no.nav.hm.grunndata.media.model

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import no.nav.hm.grunndata.dto.MediaSourceType
import no.nav.hm.grunndata.dto.MediaType
import java.time.LocalDateTime
import java.util.*

@MappedEntity("media_v1")
data class Media (
    @Id
    val uri: String,
    val origUri: String,
    val oid:    UUID,
    val order:  Int=1,
    val type: MediaType = MediaType.IMAGE,
    val text:   String?=null,
    val status: MediaStatus = MediaStatus.ACTIVE,
    val source: MediaSourceType = MediaSourceType.HMDB,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
)

enum class MediaStatus {
    ACTIVE, INACTIVE
}


