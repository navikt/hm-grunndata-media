package no.nav.hm.grunndata.media.model

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
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
class MediaHandlerTest(private val mediaRepository: MediaRepository) {

    @MockBean(StorageService::class)
    fun storageUpload(): StorageService = mockk(relaxed = true)

    @Test
    fun testMediaSync() {
        val mediaHandler = MediaHandler(mediaRepository, storageUpload())
        val oid = UUID.randomUUID()
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
            val mediaList =
                listOf(mediaRepository.save(media1), mediaRepository.save(media2), mediaRepository.save(media3))
            val dtoList = listOf(
                MediaDTO(uri = "1.jpg", oid = oid, priority = 4, text = "bilde 1", sourceUri = "1.jpg"),
                MediaDTO(uri = "4.jpg", oid = oid, priority = 4, text = "bilde 4", sourceUri = "4.jpg"),
                MediaDTO(uri = "5.jpg", oid = oid, priority = 5, text = "bilde 5", sourceUri = "5.jpg")
            )
            mediaHandler.compareAndPersistMedia(dtoList, mediaList, oid)
            val inDb = mediaRepository.findByMediaIdOid(oid)
            inDb.shouldNotBeNull()
            inDb.size shouldBe 5
            inDb.count { it.status == MediaStatus.ACTIVE } shouldBe 3
            inDb.count { it.status == MediaStatus.INACTIVE } shouldBe 2
            inDb.filter { it.status == MediaStatus.INACTIVE }
                .sortedBy { it.mediaId.uri }
                .first().mediaId.uri shouldBe "2.jpg"

        }
    }
}
