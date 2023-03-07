package no.nav.hm.grunndata.media.storage

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import no.nav.hm.grunndata.media.sync.UknownMediaSource
import no.nav.hm.grunndata.rapid.dto.MediaDTO
import no.nav.hm.grunndata.rapid.dto.MediaType
import java.net.URI
import java.util.*

@Controller("/api/v1/media")
class UploadMediaController(private val storageService: StorageService) {

    @Post(
        value = "/files/{oid}",
        consumes = [io.micronaut.http.MediaType.MULTIPART_FORM_DATA],
        produces = [io.micronaut.http.MediaType.TEXT_PLAIN]
    )
    fun uploadFile(oid: UUID, file: CompletedFileUpload): MediaDTO {
        val type = getMediaType(file)
        if (type == MediaType.OTHER) throw UknownMediaSource("only png, jpg, pdf is supported")
        val response = storageService.uploadFile(file, URI(file.name))
        return MediaDTO(oid = oid, sourceUri = file.name, uri = response.key, type = type)
    }

    private fun getMediaType(file: CompletedFileUpload): MediaType {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg", "png" -> MediaType.IMAGE
            "pdf" -> MediaType.PDF
            else -> MediaType.OTHER
        }
    }

}

val CompletedFileUpload.extension: String
    get() = name.substringAfterLast('.', "")
