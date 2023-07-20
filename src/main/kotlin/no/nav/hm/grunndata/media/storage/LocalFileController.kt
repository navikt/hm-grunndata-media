package no.nav.hm.grunndata.media.storage

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.io.File
import java.net.URI


@Controller
@Requires(env = ["local"])
class LocalFileController {


    @Get(uri  = "/local/{uri:.*}.jpg",  produces = ["image/jpeg"])
    fun getLocalMediaJPG(uri: URI) = HttpResponse.ok(File("${makeKey(uri)}.jpg").readBytes())

    @Get(uri  = "/local/{uri:.*}.png",  produces = ["image/png"])
    fun getLocalMediaPNG(uri: URI) = HttpResponse.ok(File("${makeKey(uri)}.png").readBytes())

    @Get(uri  = "/local/{uri:.*}.pdf",  produces = ["application/pdf"])
    fun getLocalMediaPDF(uri: URI) = HttpResponse.ok(File("${makeKey(uri)}.pdf").readBytes())

}