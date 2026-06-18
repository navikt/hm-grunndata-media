package no.nav.hm.grunndata.media


import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.context.annotation.Factory

import jakarta.inject.Singleton
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper

@Factory
class JacksonConfig {

    @Singleton
    fun objectMapper(): ObjectMapper {
        return JsonMapper.builderWithJackson2Defaults()
            .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
            .build()
    }

}
