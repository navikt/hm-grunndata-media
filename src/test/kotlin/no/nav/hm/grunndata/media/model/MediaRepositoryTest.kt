package no.nav.hm.grunndata.media.model

import io.kotest.common.runBlocking
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class MediaRepositoryTest(private val mediaRepository: MediaRepository) {

    @Test
    fun crudMediaRepositoryTest() {
        val uri = UUID.randomUUID().toString()
        val media = Media(uri = uri, oid = UUID.randomUUID(), text = "en tekst", size=12345, md5 = "0f0e")
        runBlocking {
            val saved = mediaRepository.save(media)
            saved.shouldNotBeNull()
            val inDb = mediaRepository.findByUri(uri)
            inDb.shouldNotBeNull()
            inDb.text shouldBe "en tekst"
            inDb.status shouldBe MediaStatus.ACTIVE
            inDb.size shouldBe  12345
            inDb.md5 shouldBe "0f0e"
        }
    }
}
