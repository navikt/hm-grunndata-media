package no.nav.hm.grunndata.media.storage

import io.micronaut.context.annotation.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("media.storage")
class MediaStorageConfig  {
    var uploadSkipDatabase: Boolean = false
    var enabled: Boolean = false
    var retention: Duration = Duration.ofMillis(300)
    var cdnurl: String = "http://localhost"

}
