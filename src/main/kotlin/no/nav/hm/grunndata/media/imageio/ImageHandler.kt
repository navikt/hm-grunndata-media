package no.nav.hm.grunndata.media.imageio

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import javax.imageio.ImageIO
import kotlin.math.min


@Singleton
class ImageHandler {

    private val small = Dimension(640, 640)
    private val medium = Dimension(1280, 1280)
    private val large = Dimension(2560, 2560)

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

    private fun resizeImage(imageUri: URI, boundary: Dimension): BufferedImage {
        val image = ImageIO.read(imageUri.toURL())
        return resizeImage(image, boundary)
    }

    private fun getScaledDimension(imageSize: Dimension, boundary: Dimension): Dimension {
        val widthRatio = boundary.getWidth() / imageSize.getWidth()
        val heightRatio = boundary.getHeight() / imageSize.getHeight()
        val ratio = min(widthRatio, heightRatio)
        return Dimension((imageSize.width * ratio).toInt(), (imageSize.height * ratio).toInt())
    }

    fun resizeSmall(imageUri: URI) = resizeImage(imageUri, small)

    fun createImageVersionInputStream(sourceUri: URI, imageVersion: String): InputStream? {
        if ("small" == imageVersion) {
            val formatName = sourceUri.path.substringAfterLast(".").lowercase()
            if ("jpg" == formatName || "png" == formatName || "gif" == formatName) {
                val bos = ByteArrayOutputStream()
                ImageIO.write(resizeSmall(sourceUri), formatName, bos)
                return ByteArrayInputStream(bos.toByteArray())
            }
        }
        return null
    }

    fun resizeMedium(imageUri: URI) = resizeImage(imageUri, medium)

    fun resizeLarge(imageUri: URI) = resizeImage(imageUri, large)


}
