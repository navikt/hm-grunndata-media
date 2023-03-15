package no.nav.hm.grunndata.media.storage

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import no.nav.hm.grunndata.media.model.*
import no.nav.hm.grunndata.media.storage.UploadMediaController.Companion.V1_UPLOAD_MEDIA
import no.nav.hm.grunndata.media.sync.UknownMediaSource
import no.nav.hm.grunndata.rapid.dto.MediaDTO
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

@Controller(V1_UPLOAD_MEDIA)
class UploadMediaController(private val storageService: StorageService,
                            private val mediaRepository: MediaRepository) {

    companion object {
        const val V1_UPLOAD_MEDIA = "/api/v1/media"
        private val LOG = LoggerFactory.getLogger(UploadMediaController::class.java)
    }

    @Post(
        value = "/file/{oid}",
        consumes = [io.micronaut.http.MediaType.MULTIPART_FORM_DATA],
        produces = [io.micronaut.http.MediaType.APPLICATION_JSON]
    )
    suspend fun uploadFile(oid: UUID,
                           file: CompletedFileUpload): HttpResponse<MediaDTO> {
        return uploadToGCS(file, oid)
    }

    private suspend fun uploadToGCS(file: CompletedFileUpload,
                                    oid: UUID): HttpResponse<MediaDTO> {
        val type = getMediaType(file)
        if (type == MediaType.OTHER) throw UknownMediaSource("only png, jpg, pdf is supported")

        val uri = "${oid}/${UUID.randomUUID()}.${file.extension}"
        LOG.info("Got file ${file.filename} with uri: $uri and size: ${file.size} for $oid")

        val response = storageService.uploadFile(file, URI(uri))
        return HttpResponse.created(
            mediaRepository.save(
                Media(
                    mediaId = MediaId(oid = oid, uri = uri),
                    sourceUri = uri,
                    type = type,
                    size = response.size,
                    status = MediaStatus.ACTIVE,
                    md5 = response.md5hash,
                    source = MediaSourceType.REGISTER // should only come from register
                )
            ).toDTO()
        )
    }

    @Post(
        value = "/files/{oid}",
        consumes = [io.micronaut.http.MediaType.MULTIPART_FORM_DATA],
        produces = [io.micronaut.http.MediaType.APPLICATION_JSON]
    )
    suspend fun uploadFiles(oid: UUID,
                            files: Publisher<CompletedFileUpload>) {
        files.asFlow().onEach {
            uploadToGCS(it, oid)
        }.collect()
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
