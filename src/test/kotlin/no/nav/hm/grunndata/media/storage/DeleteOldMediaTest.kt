package no.nav.hm.grunndata.media.storage

import io.kotest.common.runBlocking
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.hm.grunndata.media.model.Media
import no.nav.hm.grunndata.media.model.MediaId
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.MediaStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class DeleteOldMediaTest(
    private val deleteOldMedia: DeleteOldMedia,
    private val mediaRepository: MediaRepository
) {

    val oid = UUID.randomUUID()

    @MockBean(StorageService::class)
    fun storageService(): StorageService = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        runBlocking {

            val media1 = Media(
                mediaId = MediaId(
                    uri = "123.jpg",
                    oid = oid
                ),
                size = 1,
                md5 = "1",
                sourceUri = "1.jpg"
            )
            mediaRepository.save(media1)
            val media2 = Media(
                mediaId = MediaId(
                    uri = "124.jpg",
                    oid = oid
                ),
                size = 2,
                md5 = "2",
                sourceUri = "2.jpg"
            )
            mediaRepository.save(media2)
            val media3 = Media(
                mediaId = MediaId(
                    uri = "125.jpg",
                    oid = oid
                ),
                size = 3,
                md5 = "3",
                sourceUri = "3.jpg"
            )
            mediaRepository.save(media3)

            mediaRepository.update(
                media3.copy(
                    updated = LocalDateTime.now().minusDays(100),
                    status = MediaStatus.INACTIVE
                )
            )
        }
    }

    @Test
    fun deleteOldMedia() {
        deleteOldMedia.deleteOldFiles()
        runBlocking {
            mediaRepository.findByMediaIdOid(oid).size shouldBe 2
        }
    }
}
