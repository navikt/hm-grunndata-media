package no.nav.hm.grunndata.media.storage

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import no.nav.hm.grunndata.rapid.dto.MediaDTO

@Controller("/api/v1/media")
class UploadMediaController(private val storageService: StorageService) {

    @Post(value = "/file", consumes = [MediaType.MULTIPART_FORM_DATA], produces = [MediaType.TEXT_PLAIN])
    fun uploadFile(file: CompletedFileUpload): HttpResponse<MediaDTO> {
        TODO("Not yet implemented")
    }
    
}
