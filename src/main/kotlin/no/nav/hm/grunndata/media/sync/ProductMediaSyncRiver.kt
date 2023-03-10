package no.nav.hm.grunndata.media.sync

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.media.model.MediaRepository
import no.nav.hm.grunndata.rapid.dto.ProductDTO
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class MediaSyncRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val mediaRepository: MediaRepository,
    private val mediaHandler: MediaHandler
) : River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementMediaSyncRiver::class.java)
    }

    init {
        LOG.info("Using Rapid DTO version $rapidDTOVersion")
        river
            .validate { it.demandValue("createdBy", "GDB") }
            .validate { it.demandValue("payloadType", ProductDTO::class.java.simpleName) }
            .validate { it.demandKey("eventId") }
            .validate { it.demandKey("payload") }
            .validate { it.demandKey("dtoVersion") }
            .validate { it.demandKey("createdTime") }
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eventId = packet["eventId"].asText()
        val dtoVersion = packet["dtoVersion"].asLong()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], ProductDTO::class.java)
        val createdTime = packet["createdTime"].asLocalDateTime()
        LOG.info("Got eventId: $eventId for product ${dto.id} createdTime: $createdTime")
        runBlocking {
            val mediaStateList = mediaRepository.findByMediaIdOid(dto.id).sortedBy { it.updated }
            val dtoMediaList = dto.media
            if (mediaStateList.isEmpty() || createdTime.isAfter(mediaStateList.last().updated)) {
                mediaHandler.compareAndPersistMedia(dtoMediaList, mediaStateList, dto.id, dto)
            } else {
                LOG.info("Skip this event cause eventTime: $createdTime is older than ${mediaStateList.last().updated}")
            }
        }
    }

}
