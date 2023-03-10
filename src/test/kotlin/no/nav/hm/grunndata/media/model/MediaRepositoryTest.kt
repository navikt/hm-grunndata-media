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
        val oid = UUID.randomUUID()
        val media = Media(mediaId = MediaId(uri = uri, oid = oid), size = 12345, md5 = "0f0e", sourceUri = uri)
        runBlocking {
            val saved = mediaRepository.save(media)
            saved.shouldNotBeNull()
            val inDb = mediaRepository.findById(MediaId(oid, uri))
            inDb.shouldNotBeNull()
            inDb.status shouldBe MediaStatus.ACTIVE
            inDb.size shouldBe 12345
            inDb.md5 shouldBe "0f0e"

            val list = mediaRepository.findByMediaIdOid(oid)
            list.size shouldBe 1
        }
    }
}
