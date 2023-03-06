package no.nav.hm.grunndata.media.storage

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.temporal.ChronoUnit

@MicronautTest
class MediaStorageDefaultConfigTest(
    private val mediaStorageConfig: MediaStorageConfig
) {

    @Test
    fun testDefaultConfigs() {
        mediaStorageConfig.enabled shouldBe false
        mediaStorageConfig.retention shouldBe Duration.of(90, ChronoUnit.DAYS)
    }


}
