package no.nav.hm.grunndata.media.model

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class MediaUriRepositoryTest(private val mediaUriRepository: MediaUriRepository ) {


    @Test
    fun crudTest() {
        runBlocking {
            val uuid = UUID.randomUUID()
            val uri = "/register/123/321.jpg"
            val saved = mediaUriRepository.save(
                MediaUri(
                    uri = uri,
                    sourceUri = "http://source.com/123.jpg",
                    oid = uuid,
                    md5 = "14FE0532DB",
                    size = 1234L,
                    filename = "321.jpg",
                    type = MediaType.IMAGE,
                    source = MediaSourceType.REGISTER,
                    objectType = ObjectType.SERIES
                )
            )

            saved.shouldNotBeNull()
            val found = mediaUriRepository.findByUri(uri)
            found.shouldNotBeNull()
            found.status shouldBe MediaStatus.ACTIVE
            val updated = mediaUriRepository.update(
                found.copy(
                    status = MediaStatus.INACTIVE
                )
            )
            updated.status shouldBe MediaStatus.INACTIVE
        }
    }

}