package no.nav.hm.grunndata.media.imageio

import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.media.storage.MediaUploadTest
import java.io.File
import javax.imageio.ImageIO

@MicronautTest
class ImageHandlerTest(private val imageHandler: ImageHandler) {

    //@Test
    fun testImageResize() {
        val imageUri = MediaUploadTest::class.java.classLoader.getResource("66131.jpg").toURI()
        val formatName = imageUri.path.substringAfterLast(".")
        ImageIO.write(imageHandler.resizeSmall(imageUri), formatName, File("small.$formatName"))
        ImageIO.write(imageHandler.resizeMedium(imageUri), formatName, File("medium.$formatName"))
        ImageIO.write(imageHandler.resizeLarge(imageUri), formatName, File("large.$formatName"))
        imageHandler.createImageVersionInputStream(imageUri, "small").shouldNotBeNull()
    }

}
