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
import javax.imageio.ImageReader
import kotlin.math.min


@Singleton
class ImageHandler {

    companion object {
        private val LOG = LoggerFactory.getLogger(ImageHandler::class.java)
        val SMALL = Dimension(400, 400)
        val MEDIUM = Dimension(800, 800)
        val LARGE = Dimension(1600, 1600)

    }

    init {
        val readers: Iterator<ImageReader> = ImageIO.getImageReadersByFormatName("JPEG")
        while (readers.hasNext()) {
            LOG.info("reader: " + readers.next())
        }
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

    fun resizeSmall(imageUri: URI) = resizeImage(imageUri, SMALL)

    fun createImageVersionInputStream(sourceUri: URI, imageVersion: Dimension): InputStream? {
        if (SMALL == imageVersion) {
            val formatName = sourceUri.path.substringAfterLast(".").lowercase()
            if ("jpg" == formatName || "png" == formatName || "gif" == formatName) {
                val bos = ByteArrayOutputStream()
                ImageIO.write(resizeSmall(sourceUri), formatName, bos)
                return ByteArrayInputStream(bos.toByteArray())
            }
        }
        return null
    }

    fun resizeMedium(imageUri: URI) = resizeImage(imageUri, MEDIUM)

    fun resizeLarge(imageUri: URI) = resizeImage(imageUri, LARGE)


}
