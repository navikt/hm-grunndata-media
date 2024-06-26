package no.nav.hm.grunndata.media.storage

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.multipart.CompletedFileUpload
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import no.nav.hm.grunndata.media.model.MediaStatus
import no.nav.hm.grunndata.media.model.MediaUri
import no.nav.hm.grunndata.media.model.MediaUriDTO
import no.nav.hm.grunndata.media.model.MediaUriRepository
import no.nav.hm.grunndata.media.model.ObjectType
import no.nav.hm.grunndata.media.model.toDTO
import no.nav.hm.grunndata.media.storage.UploadMediaController.Companion.V1_UPLOAD_MEDIA
import no.nav.hm.grunndata.media.sync.BadRequestException
import no.nav.hm.grunndata.media.sync.UknownMediaSource
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

@Controller(V1_UPLOAD_MEDIA)
class UploadMediaController(
    private val storageService: StorageService,
    private val mediaUriRepository: MediaUriRepository,
    private val mediaStorageConfig: MediaStorageConfig
) {

    companion object {
        const val V1_UPLOAD_MEDIA = "/api/v1/upload/media"
        const val REGISTER_UPLOAD_PREFIX = "register"
        const val IMPORT_UPLOAD_PREFIX = "import"
        private val LOG = LoggerFactory.getLogger(UploadMediaController::class.java)
    }

    @Get("/{app}/oid/{oid}")
    suspend fun getMediaByOid(app: String, oid: UUID): List<MediaUriDTO> {
        LOG.info("Got request for media list of oid $oid from $app")
        return mediaUriRepository.findByOid(oid).map { it.toDTO() }
    }

    @Post(
        uri = "/{app}/file/{oid}",
        consumes = [io.micronaut.http.MediaType.MULTIPART_FORM_DATA],
        produces = [io.micronaut.http.MediaType.APPLICATION_JSON]
    )
    suspend fun uploadFile(
        oid: UUID, app: String,
        file: CompletedFileUpload, @QueryValue objectType: ObjectType = ObjectType.PRODUCT
    ): MediaUriDTO {
        return uploadToStorage(file, app, oid, objectType)
    }


    @Delete("/{app}/{oid}/{uri}")
    suspend fun deleteByOidAndUri(app: String, oid: UUID, uri: String): MediaUriDTO =
        mediaUriRepository.findByOidAndUri(oid, uri)?.let {
            LOG.info("Got deleted request from $app for $oid and $uri")
            mediaUriRepository.update(it.copy(status = MediaStatus.DELETED, updated = LocalDateTime.now())).toDTO()
        } ?: throw BadRequestException("Not found $oid and $uri")


    @Post(
        uri = "/{app}/files/{oid}",
        consumes = [io.micronaut.http.MediaType.MULTIPART_FORM_DATA],
        produces = [io.micronaut.http.MediaType.APPLICATION_JSON]
    )
    suspend fun uploadFiles(
        oid: UUID, app: String,
        files: Publisher<CompletedFileUpload>
    ): List<MediaUriDTO> =
        files.asFlow().map { uploadToStorage(it, app, oid) }.toList()


    private suspend fun uploadToStorage(
        file: CompletedFileUpload, app: String,
        oid: UUID, objectType: ObjectType = ObjectType.UNKNOWN
    ): MediaUriDTO {
        LOG.info("Got request to upload file for ${file.name} app: $app, oid: $oid and objectType: $objectType")
        val type = getMediaType(file)
        if (type == MediaType.OTHER) throw UknownMediaSource("only png, jpg, pdf is supported")
        val extension = getMediaExtension(file.extension)

        val id = UUID.randomUUID()
        val uri = "$app/${oid}/${id}.$extension"
        LOG.info("Got file ${file.filename} with uri: $uri and size: ${file.size} for $oid")
        val source = if (app == REGISTER_UPLOAD_PREFIX) MediaSourceType.REGISTER else MediaSourceType.IMPORT
        val response = storageService.uploadFile(file, URI(uri))
        return mediaUriRepository.save(
            MediaUri(
                oid = oid,
                filename = file.filename,
                uri = uri,
                sourceUri = "${mediaStorageConfig.cdnurl}/$uri",
                type = type,
                objectType = objectType,
                size = response.size,
                status = MediaStatus.ACTIVE,
                md5 = response.md5hash,
                source = source
            )
        ).toDTO()
    }

    private fun getMediaExtension(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "jpg"
            "png" -> "png"
            "pdf" -> "pdf"
            else -> extension
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
