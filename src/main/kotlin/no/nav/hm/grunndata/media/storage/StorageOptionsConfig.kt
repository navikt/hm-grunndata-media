package no.nav.hm.grunndata.media.storage

import com.google.cloud.http.HttpTransportOptions
import com.google.cloud.storage.StorageOptions
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import jakarta.inject.Singleton

@Singleton
class StorageOptionsConfig : BeanCreatedEventListener<StorageOptions.Builder> {
    override fun onCreated(event: BeanCreatedEvent<StorageOptions.Builder>): StorageOptions.Builder {
        val storageOptionsbuilder = event.bean
        storageOptionsbuilder.setTransportOptions(HttpTransportOptions.newBuilder().apply {
            setConnectTimeout(60000)
            setReadTimeout(60000)
        }.build())
        return storageOptionsbuilder
    }

}
