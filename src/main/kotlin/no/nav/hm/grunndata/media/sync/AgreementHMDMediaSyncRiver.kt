package no.nav.hm.grunndata.media.sync

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.River
import no.nav.hm.grunndata.media.model.MediaUriRepository
import no.nav.hm.grunndata.media.model.ObjectType
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.grunndata.rapid.event.RapidApp
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class AgreementHMDMediaSyncRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val mediaUriRepository: MediaUriRepository,
    private val mediaUriHandler: MediaUriHandler,
) : River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementHMDMediaSyncRiver::class.java)
    }

    init {
        LOG.info("Using Rapid DTO version $rapidDTOVersion")
        river
            .validate { it.demandValue("createdBy", RapidApp.grunndata_db) }
            .validate { it.demandAny("eventName", listOf(EventName.hmdbagreementsyncV1))}
            .validate { it.demandKey("eventId") }
            .validate { it.demandKey("payload") }
            .validate { it.demandKey("dtoVersion") }
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val eventId = packet["eventId"].asText()

        val dtoVersion = packet["dtoVersion"].asLong()
        if (dtoVersion > rapidDTOVersion) LOG.warn("dto version $dtoVersion is newer than $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], AgreementDTO::class.java)
        LOG.info("Got eventId: $eventId for agreement ${dto.id}")
        runBlocking {
            mediaUriRepository.deleteByOid(dto.id)
            val inDbList = mediaUriRepository.findByOid(dto.id)
            val mediaInfoList = dto.attachments.flatMap { it.media }.toSet()
            mediaUriHandler.compareAndPersistMedia(mediaInfoList, inDbList, dto.id, ObjectType.AGREEMENT)
        }
    }

}
