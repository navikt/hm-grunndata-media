package no.nav.hm.grunndata.media.model

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.mockk
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.hm.grunndata.media.storage.StorageUpload
import no.nav.hm.grunndata.media.sync.AgreementMediaSyncRiver
import no.nav.hm.grunndata.rapid.dto.MediaDTO
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class MediaSyncRiverTest(private val objectMapper: ObjectMapper, private val mediaRepository: MediaRepository) {

    @MockBean(StorageUpload::class)
    fun storageUpload(): StorageUpload = mockk(relaxed = true)

    @MockBean(RapidsConnection::class)
    fun kafkaRapid(): RapidsConnection = mockk(relaxed = true)

    @Test
    fun testMediaSync() {
        val mediaSyncRiver = AgreementMediaSyncRiver(
            river = RiverHead(kafkaRapid()),
            storageUpload = storageUpload(),
            objectMapper = objectMapper,
            mediaRepository = mediaRepository
        )

        val oid = UUID.randomUUID()
        runBlocking {
            val media1 = Media(uri = "1.jpg", oid = oid, priority = 1, text = "bilde 1", size = 1, md5 = "1")
            val media2 = Media(uri = "2.jpg", oid = oid, priority = 2, text = "bilde 2", size = 2, md5 = "2")
            val media3 = Media(uri = "3.jpg", oid = oid, priority = 3, text = "bilde 3", size = 3, md5 = "3")
            val mediaList =
                listOf(mediaRepository.save(media1), mediaRepository.save(media2), mediaRepository.save(media3))
            val dtoList = listOf(
                MediaDTO(uri = "1.jpg", oid = oid, priority = 4, text = "bilde 1"),
                MediaDTO(uri = "4.jpg", oid = oid, priority = 4, text = "bilde 4"),
                MediaDTO(uri = "5.jpg", oid = oid, priority = 5, text = "bilde 5")
            )
            mediaSyncRiver.compareAndPersistMedia(dtoList, mediaList, oid)
            val inDb = mediaRepository.findByOid(oid)
            inDb.shouldNotBeNull()
            inDb.size shouldBe 5
            inDb.count { it.status == MediaStatus.ACTIVE } shouldBe 3
            inDb.count { it.status == MediaStatus.INACTIVE } shouldBe 2
            inDb.filter { it.status == MediaStatus.INACTIVE }.sortedBy { it.uri }.first().uri shouldBe "2.jpg"

        }
    }
}
