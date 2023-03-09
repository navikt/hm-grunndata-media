package no.nav.hm.grunndata.media.storage

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.security.annotation.Secured
import no.nav.hm.grunndata.media.model.Media
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.MediaStatus
import no.nav.hm.grunndata.media.model.toDTO
import no.nav.hm.grunndata.media.storage.UploadMediaController.Companion.V1_UPLOAD_MEDIA
import no.nav.hm.grunndata.media.sync.UknownMediaSource
import no.nav.hm.grunndata.rapid.dto.MediaDTO
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

@Secured(Roles.ROLE_ADMIN)
@Controller(V1_UPLOAD_MEDIA)
class UploadMediaController(
    private val storageService: StorageService,
    private val mediaRepository: MediaRepository
) {

    companion object {
        const val V1_UPLOAD_MEDIA = "/v1/files"
        private val LOG = LoggerFactory.getLogger(UploadMediaController::class.java)
    }

    @Post(
        value = "/{oid}",
        consumes = [io.micronaut.http.MediaType.MULTIPART_FORM_DATA],
        produces = [io.micronaut.http.MediaType.TEXT_PLAIN]
    )
    suspend fun uploadFile(oid: UUID, file: CompletedFileUpload): MediaDTO {
        val type = getMediaType(file)
        if (type == MediaType.OTHER) throw UknownMediaSource("only png, jpg, pdf is supported")

        val uri = "${oid}_${UUID.randomUUID()}.${file.extension}"
        LOG.info("Got file ${file.filename} with uri: $uri and size: ${file.size} for $oid")

        val response = storageService.uploadFile(file, URI(uri))
        return mediaRepository.save(
            Media(
                oid = oid,
                sourceUri = uri,
                uri = uri,
                type = type,
                size = response.size,
                status = MediaStatus.ACTIVE,
                md5 = response.md5hash,
                source = MediaSourceType.REGISTER // should only come from register
            )
        ).toDTO()
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
    get() = filename.substringAfterLast('.', "")

object Roles {
    const val ROLE_ADMIN = "ROLE_ADMIN"
    const val ROLE_SUPPLIER = "ROLE_SUPPLIER"
}
