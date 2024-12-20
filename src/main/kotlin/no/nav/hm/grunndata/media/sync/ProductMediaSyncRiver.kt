package no.nav.hm.grunndata.media.sync

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import no.nav.hm.grunndata.media.model.MediaUriRepository
import no.nav.hm.grunndata.media.model.ObjectType
import no.nav.hm.grunndata.media.storage.MediaStorageConfig
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.dto.SeriesRapidDTO
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.grunndata.rapid.event.RapidApp
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class MediaSyncRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val mediaUriRepository: MediaUriRepository,
    private val mediaUriHandler: MediaUriHandler,
    private val mediaStorageConfig: MediaStorageConfig,
) : River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(MediaSyncRiver::class.java)
    }

    init {
        LOG.info("Using Rapid DTO version $rapidDTOVersion")
        river
            .validate { it.demandValue("createdBy", RapidApp.grunndata_db) }
            .validate { it.demandAny("eventName", listOf(EventName.syncedRegisterSeriesV1)) }
            .validate { it.demandKey("eventId") }
            .validate { it.demandKey("payload") }
            .validate { it.demandKey("dtoVersion") }
            .validate { it.demandKey("createdTime") }
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eventId = packet["eventId"].asText()
        val dtoVersion = packet["dtoVersion"].asLong()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version: $dtoVersion is newer than our version: $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], SeriesRapidDTO::class.java)
        val createdTime = packet["createdTime"].asLocalDateTime()
        LOG.info("Got eventId: $eventId for product ${dto.id} createdTime: $createdTime")
        runBlocking {
            val dtoMediaList = dto.seriesData?.media?.toSet() ?: emptySet()
            val mediaStateList = mediaUriRepository.findByOid(dto.id).sortedBy { it.updated }
            if (mediaStateList.isEmpty() || createdTime.isAfter(mediaStateList.last().updated)) {
                mediaUriHandler.compareAndPersistMedia(dtoMediaList, mediaStateList, dto.id, ObjectType.SERIES)
            } else {
                LOG.info("Skip this event cause event created time : $createdTime is older than ${mediaStateList.last().updated}")
            }

        }
    }

}
