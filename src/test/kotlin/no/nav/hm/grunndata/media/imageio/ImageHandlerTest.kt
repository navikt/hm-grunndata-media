package no.nav.hm.grunndata.media.imageio

import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.media.imageio.ImageHandler.Companion.SMALL
import org.junit.jupiter.api.Test

@MicronautTest
class ImageHandlerTest(private val imageHandler: ImageHandler) {

    @Test
    fun testImageResize() {
        val imageUri = ImageHandlerTest::class.java.classLoader.getResource("66131.jpg").toURI()
        imageHandler.createImageVersionInputStream(imageUri, SMALL).shouldNotBeNull()
    }

}
