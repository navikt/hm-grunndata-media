package no.nav.hm.grunndata.media.model

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.hm.grunndata.media.imageio.ImageHandler
import no.nav.hm.grunndata.media.storage.StorageService
import no.nav.hm.grunndata.media.sync.MediaHandler
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class AllowReuseOfMediaTest(private val mediaRepository: MediaRepository, private val imageHandler: ImageHandler) {

    @MockBean(StorageService::class)
    fun storageUpload(): StorageService = mockk(relaxed = true)

    val mediaHandler = MediaHandler(mediaRepository, storageUpload(), imageHandler)

    @Test
    fun mediaShouldBeAbleReuseUris() {
        val oid = UUID.randomUUID()
        val oid2 = UUID.randomUUID()
        runBlocking {
            val media1 = Media(
                mediaId = MediaId(
                    uri = "1.jpg",
                    oid = oid
                ),
                size = 1,
                md5 = "1",
                sourceUri = "1.jpg"
            )
            val media2 = Media(
                mediaId = MediaId(
                    uri = "2.jpg",
                    oid = oid
                ),
                size = 2,
                md5 = "2",
                sourceUri = "2.jpg"
            )
            val media3 = Media(
                mediaId = MediaId(
                    uri = "3.jpg",
                    oid = oid
                ),
                size = 3,
                md5 = "3",
                sourceUri = "3.jpg"
            )
            val inDbList1 =
                listOf(mediaRepository.save(media1), mediaRepository.save(media2), mediaRepository.save(media3))

            inDbList1.size shouldBe 3

            val mediaInfoList = listOf(
                MediaInfo(uri = "1.jpg", priority = 4, text = "bilde 1", sourceUri = "1.jpg"),
                MediaInfo(uri = "4.jpg", priority = 4, text = "bilde 4", sourceUri = "4.jpg"),
                MediaInfo(uri = "5.jpg", priority = 5, text = "bilde 5", sourceUri = "5.jpg")
            )
            val inDbList2 = mediaRepository.findByMediaIdOid(oid2)
            inDbList2.size shouldBe 0
            mediaHandler.compareAndPersistMedia(mediaInfoList, inDbList2, oid2)
            mediaRepository.findById(MediaId(oid2, "1.jpg")).shouldNotBeNull()
            mediaRepository.findByMediaIdOid(oid2).size shouldBe 3
            mediaRepository.findByMediaIdUri("1.jpg").size shouldBe 2

        }
    }
}
