package no.nav.hm.grunndata.media

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.River
import no.nav.hm.grunndata.media.model.Media
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.media.model.MediaStatus
import no.nav.hm.grunndata.media.storage.StorageUpload
import no.nav.hm.grunndata.rapid.dto.MediaDTO
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.ProductDTO
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDateTime

@Context
@Requires(bean = KafkaRapid::class)
class MediaSyncRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val storageUpload: StorageUpload,
    private val mediaRepository: MediaRepository
) : River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(MediaSyncRiver::class.java)
    }

    init {
        LOG.info("Using Rapid DTO version $rapidDTOVersion")
        river
            .validate { it.demandValue("createdBy", "GDB") }
            .validate { it.demandValue("payloadType", ProductDTO::class.java.simpleName) }
            .validate { it.demandKey("eventId") }
            .validate { it.demandKey("payload") }
            .validate { it.demandKey("dtoVersion") }
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eventId = packet["eventId"].asText()
        val dtoVersion = packet["dtoVersion"].asLong()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], ProductDTO::class.java)
        runBlocking {
            val mediaStateList = mediaRepository.findByOid(dto.id)
            val dtoMediaList = dto.media
            compareAndPersistMedia(dtoMediaList, mediaStateList, dto)
        }
    }

    private suspend fun compareAndPersistMedia(
        dtoMediaList: List<MediaDTO>,
        mediaStateList: List<Media>,
        dto: ProductDTO
    ) {
        val newMediaList = dtoMediaList.filter { m -> mediaStateList.none { m.uri == it.uri } }
        val notInUseList = mediaStateList.filter { n -> dtoMediaList.none { n.uri == it.uri } }
        notInUseList.forEach {
            mediaRepository.update(it.copy(status = MediaStatus.INACTIVE, updated = LocalDateTime.now()))
        }
        newMediaList.forEach {
            // upload and save
            val upload = storageUpload.uploadStream(buildUri(it))
            mediaRepository.save(
                Media(
                    uri = it.uri, oid = dto.id, size = 0, type = it.type,
                    priority = it.priority, source = it.source, text = it.text, md5 = "123"
                )
            )
        }
    }

    private fun buildUri(media: MediaDTO): URI {
        if (media.source == MediaSourceType.HMDB) {
            return URI("http")
        }
        throw Exception()
    }

}
