package no.nav.hm.grunndata.media.imageio

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.net.URI
import javax.imageio.ImageIO
import kotlin.math.min


@Singleton
class ImageHandler {

    val small = Dimension(640, 640)
    val medium = Dimension(1280, 1280)

    companion object {
        private val LOG = LoggerFactory.getLogger(ImageHandler::class.java)
    }

    private fun resizeImage(image: BufferedImage, boundary: Dimension): BufferedImage {
        val imageDimension = Dimension(image.width, image.height)
        LOG.info("Image dimensions $imageDimension")
        val scaled = getScaledDimension(imageDimension, boundary)
        val resizedImage = BufferedImage(scaled.width, scaled.height, BufferedImage.TYPE_INT_RGB)
        val graphics2D = resizedImage.createGraphics()
        graphics2D.drawImage(image, 0, 0, scaled.width, scaled.height, null)
        graphics2D.dispose()
        return resizedImage
    }

    fun resizeImage(imageUri: URI, boundary: Dimension): BufferedImage {
        val image = ImageIO.read(imageUri.toURL())
        return resizeImage(image, boundary)
    }

    fun getScaledDimension(imageSize: Dimension, boundary: Dimension): Dimension {
        val widthRatio = boundary.getWidth() / imageSize.getWidth()
        val heightRatio = boundary.getHeight() / imageSize.getHeight()
        val ratio = min(widthRatio, heightRatio)
        return Dimension((imageSize.width * ratio).toInt(), (imageSize.height * ratio).toInt())
    }

    fun resizeSmall(imageUri: URI) = resizeImage(imageUri, small)

    fun resizeMedium(imageUri: URI) = resizeImage(imageUri, medium)
}
