package no.nav.hm.grunndata.media.imageio

import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.media.imageio.ImageHandler.Companion.SMALL
import no.nav.hm.grunndata.media.storage.MediaUploadTest
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

@MicronautTest
class ImageHandlerTest(private val imageHandler: ImageHandler) {

    @Test
    fun testImageResize() {
        val imageUri = MediaUploadTest::class.java.classLoader.getResource("64341_4.jpg").toURI()
        val formatName = imageUri.path.substringAfterLast(".")
        ImageIO.write(imageHandler.resizeSmall(imageUri), formatName, File("small.$formatName"))
        ImageIO.write(imageHandler.resizeMedium(imageUri), formatName, File("medium.$formatName"))
        ImageIO.write(imageHandler.resizeLarge(imageUri), formatName, File("large.$formatName"))
        imageHandler.createImageVersionInputStream(imageUri, SMALL).shouldNotBeNull()
    }

}
