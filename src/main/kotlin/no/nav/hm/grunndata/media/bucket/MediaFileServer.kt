package no.nav.hm.grunndata.media.bucket

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.server.types.files.StreamedFile
import no.nav.hm.grunndata.media.storage.GCStorageStorageService
import java.net.URI

@Controller("/internal/media/file")
class MediaFileServer(private val gcStorageStorageService: GCStorageStorageService) {

    @Get("/{uri:.*}", produces = [MediaType.APPLICATION_OCTET_STREAM])
    suspend fun getFile(uri: String): HttpResponse<StreamedFile> {
        val blob = gcStorageStorageService.readFile(URI.create(uri))
        return HttpResponse.ok(StreamedFile(blob.getContent().inputStream(), MediaType.of(blob.contentType)))
    }
}