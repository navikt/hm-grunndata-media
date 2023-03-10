package no.nav.hm.grunndata.media

import io.micronaut.runtime.Micronaut

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.nav.hm.grunndata.media")
            .mainClass(Application.javaClass)
            .start()
    }
}
