package no.nav.hm.grunndata.media.model

import io.kotest.common.runBlocking
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.hm.grunndata.media.storage.StorageService
import no.nav.hm.grunndata.media.sync.MediaHandler
import no.nav.hm.grunndata.rapid.dto.MediaDTO
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class AllowReuseOfMediaTest(private val mediaRepository: MediaRepository) {

    @MockBean(StorageService::class)
    fun storageUpload(): StorageService = mockk(relaxed = true)

    val mediaHandler = MediaHandler(mediaRepository, storageUpload())

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
            mediaRepository.saveAll(listOf(media1, media2, media3))
            val inDbList1 = mediaRepository.findByMediaIdOid(oid)
            inDbList1.size shouldBe 3

            val dtoList = listOf(
                MediaDTO(uri = "1.jpg", oid = oid2, priority = 4, text = "bilde 1", sourceUri = "1.jpg"),
                MediaDTO(uri = "4.jpg", oid = oid2, priority = 4, text = "bilde 4", sourceUri = "4.jpg"),
                MediaDTO(uri = "5.jpg", oid = oid2, priority = 5, text = "bilde 5", sourceUri = "5.jpg")
            )
            val inDbList2 = mediaRepository.findByMediaIdOid(oid2)
            mediaHandler.compareAndPersistMedia(dtoList, inDbList2, oid2)

        }
    }
}
