package no.nav.hm.grunndata.media.storage

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.util.Toggleable
import java.time.Duration

@ConfigurationProperties("media.storage")
class MediaStorageConfig : Toggleable {
    var enabled: Boolean = false
    var retention: Duration = Duration.ofMillis(300)

}
