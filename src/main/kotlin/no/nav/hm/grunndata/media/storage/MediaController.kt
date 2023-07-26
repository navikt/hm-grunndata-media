package no.nav.hm.grunndata.media.storage

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import no.nav.hm.grunndata.media.model.*
import no.nav.hm.grunndata.media.storage.UploadMediaController.Companion.V1_UPLOAD_MEDIA
import no.nav.hm.grunndata.media.sync.BadRequestException
import no.nav.hm.grunndata.media.sync.UknownMediaSource
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDateTime
import java.util.*

@Controller(V1_UPLOAD_MEDIA)
class UploadMediaController(private val storageService: StorageService,
                            private val mediaRepository: MediaRepository,
                            private val mediaStorageConfig: MediaStorageConfig) {

    companion object {
        const val V1_UPLOAD_MEDIA = "/api/v1/upload/media"
        const val REGISTER_UPLOAD_PREFIX = "register"
        const val IMPORT_UPLOAD_PREFIX="import"
        private val LOG = LoggerFactory.getLogger(UploadMediaController::class.java)
    }

    @Get("/oid/{oid}")
    suspend fun getMediaByOid(oid: UUID): List<MediaDTO> = mediaRepository.findByOid(oid).map { it.toDTO() }


    @Post(
        uri = "/{app}/file/{oid}",
        consumes = [io.micronaut.http.MediaType.MULTIPART_FORM_DATA],
        produces = [io.micronaut.http.MediaType.APPLICATION_JSON]
    )
    suspend fun uploadFile(oid: UUID, app: String,
                           file: CompletedFileUpload): MediaDTO {
        return uploadToStorage(file, app, oid)
    }

    private suspend fun uploadToStorage(file: CompletedFileUpload, app:String,
                                        oid: UUID): MediaDTO {
        val type = getMediaType(file)
        if (type == MediaType.OTHER) throw UknownMediaSource("only png, jpg, pdf is supported")
        if ("register"!=app) throw BadRequestException("Not allowed, only register is supported!")

        val id = UUID.randomUUID()
        val uri = "$REGISTER_UPLOAD_PREFIX/${oid}/${id}.${file.extension}"
        LOG.info("Got file ${file.filename} with uri: $uri and size: ${file.size} for $oid")

        val response = storageService.uploadFile(file, URI(uri))
        return mediaRepository.save(
            Media(
                id = id,
                oid = oid,
                uri = uri,
                sourceUri = "${mediaStorageConfig.cdnurl}/$uri",
                type = type,
                size = response.size,
                status = MediaStatus.ACTIVE,
                md5 = response.md5hash,
                source = MediaSourceType.REGISTER // should only come from register
            )
        ).toDTO()
    }

    @Post(
        uri = "/{app}/files/{oid}",
        consumes = [io.micronaut.http.MediaType.MULTIPART_FORM_DATA],
        produces = [io.micronaut.http.MediaType.APPLICATION_JSON]
    )
    suspend fun uploadFiles(oid: UUID, app: String,
                            files: Publisher<CompletedFileUpload>): List<MediaDTO> =
        files.asFlow().map { uploadToStorage(it, app, oid) }.toList()


    @Delete("/{oid}/{uri}")
    suspend fun deleteByOidAndUri(oid:UUID, uri: String): MediaDTO =  mediaRepository.findByOidAndUri(oid, uri)?.let {
            mediaRepository.update(it.copy(status = MediaStatus.DELETED, updated = LocalDateTime.now())).toDTO()
        } ?: throw BadRequestException("Not found $oid and $uri")


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
