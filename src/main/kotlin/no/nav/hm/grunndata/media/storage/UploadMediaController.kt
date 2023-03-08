package no.nav.hm.grunndata.media.storage

import com.google.rpc.BadRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.media.model.Media
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.toDTO
import no.nav.hm.grunndata.media.sync.BadRequestException
import no.nav.hm.grunndata.media.sync.UknownMediaSource
import no.nav.hm.grunndata.rapid.dto.MediaDTO
import no.nav.hm.grunndata.rapid.dto.MediaType
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

@Controller("/api/v1/media")
class UploadMediaController(private val storageService: StorageService,
                            private val mediaRepository: MediaRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(UploadMediaController::class.java)
    }

    @Post(
        value = "/files/{oid}/{uri}",
        consumes = [io.micronaut.http.MediaType.MULTIPART_FORM_DATA],
        produces = [io.micronaut.http.MediaType.TEXT_PLAIN]
    )
    fun uploadFile(oid: UUID, uri: String, file: CompletedFileUpload): MediaDTO {
        LOG.info("Got file ${file.filename} with uri: $uri and size: ${file.size}")
        val type = getMediaType(file)
        if (type == MediaType.OTHER) throw UknownMediaSource("only png, jpg, pdf is supported")
        return runBlocking {
            if (mediaRepository.findByUri(uri) != null) throw BadRequestException("Duplicate, media already exist")
            val response = storageService.uploadFile(file, URI(uri))
            mediaRepository.save(
                Media(oid = oid, sourceUri = uri, uri = uri, type = type, size = response.size, md5 = response.md5hash)
            ).toDTO()
        }
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
