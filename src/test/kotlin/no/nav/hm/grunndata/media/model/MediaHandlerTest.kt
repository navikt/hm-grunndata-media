package no.nav.hm.grunndata.media.model

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.hm.grunndata.media.storage.StorageService
import no.nav.hm.grunndata.media.sync.MediaUriHandler
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.MediaType
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class MediaHandlerTest(private val mediaUriRepository: MediaUriRepository) {

    @MockBean(StorageService::class)
    fun storageUpload(): StorageService = mockk(relaxed = true)

    @Test
    fun testMediaSync() {
        val mediaHandler = MediaUriHandler(mediaUriRepository, storageUpload())
        val oid = UUID.randomUUID()
        runBlocking {
            val media1 = MediaUri(
                uri = "1.jpg",
                oid = oid,
                size = 1,
                md5 = "1",
                objectType = ObjectType.SERIES,
                type = MediaType.IMAGE,
                filename = "1.jpg",
                sourceUri = "1.jpg",
            )
            val media2 = MediaUri(
                uri = "2.jpg",
                oid = oid,
                size = 2,
                md5 = "2",
                objectType = ObjectType.SERIES,
                type = MediaType.IMAGE,
                filename = "2.jpg",
                sourceUri = "2.jpg"
            )
            val media3 = MediaUri(
                uri = "3.jpg",
                oid = oid,
                size = 3,
                md5 = "3",
                objectType = ObjectType.SERIES,
                type = MediaType.IMAGE,
                filename = "3.jpg",
                sourceUri = "3.jpg"
            )
            val media4 = MediaUri(
                uri = "4.jpg",
                oid = oid,
                size = 4,
                md5 = "4",
                sourceUri = "4.jpg",
                objectType = ObjectType.SERIES,
                type = MediaType.IMAGE,
                filename = "4.jpg",
                status = MediaStatus.INACTIVE
            )
            val mediaList =
                listOf(mediaUriRepository.save(media1), mediaUriRepository.save(media2), mediaUriRepository.save(media3),mediaUriRepository.save(media4))
            val mediaInfoList = setOf(
                MediaInfo(uri = "1.jpg", priority = 1, text = "bilde 1", sourceUri = "1.jpg"),
                MediaInfo(uri = "4.jpg", priority = 4, text = "bilde 4", sourceUri = "4.jpg"),
                MediaInfo(uri = "5.jpg", priority = 5, text = "bilde 5", sourceUri = "5.jpg"),
                MediaInfo(uri = "6.jpg", priority = 6, text = "bilde 6", sourceUri = "6.jpg")
            )
            mediaHandler.compareAndPersistMedia(mediaInfoList, mediaList, oid, ObjectType.SERIES)
            val inDb = mediaUriRepository.findByOid(oid)
            inDb.shouldNotBeNull()
            inDb.size shouldBe 6
            inDb.count { it.status == MediaStatus.ACTIVE } shouldBe 4
            inDb.count { it.status == MediaStatus.INACTIVE } shouldBe 2
            inDb.filter { it.status == MediaStatus.INACTIVE }
                .sortedBy { it.uri }
                .first().uri shouldBe "2.jpg"

        }
    }
}
