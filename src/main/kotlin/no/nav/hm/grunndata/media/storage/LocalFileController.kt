package no.nav.hm.grunndata.media.storage

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import no.nav.hm.grunndata.media.storage.FileStorageService.Companion.LOCALPATH
import java.io.File
import java.net.URI


@Controller
@Requires(env = ["local"])
@ExecuteOn(TaskExecutors.BLOCKING)
class LocalFileController {


    @Get(uri  = "/local/{uri:.*}.jpg",  produces = ["image/jpeg"])
    fun getLocalMediaJPG(uri: URI) = HttpResponse.ok(File("${LOCALPATH}/${makeKey(uri)}.jpg").readBytes())

    @Get(uri  = "/local/{uri:.*}.png",  produces = ["image/png"])
    fun getLocalMediaPNG(uri: URI) = HttpResponse.ok(File("${LOCALPATH}/${makeKey(uri)}.png").readBytes())

    @Get(uri  = "/local/{uri:.*}.pdf",  produces = ["application/pdf"])
    fun getLocalMediaPDF(uri: URI) = HttpResponse.ok(File("${LOCALPATH}/${makeKey(uri)}.pdf").readBytes())

}