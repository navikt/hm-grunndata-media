package no.nav.hm.grunndata.media.sync

import jakarta.inject.Singleton
import no.nav.hm.grunndata.media.model.Media
import no.nav.hm.grunndata.media.model.MediaUri
import no.nav.hm.grunndata.media.model.MediaUriRepository
import no.nav.hm.grunndata.media.model.ObjectType
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import org.slf4j.LoggerFactory

@Singleton
class MediaUriHandler(private val mediaUriRepository: MediaUriRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(MediaUriHandler::class.java)
    }
    suspend fun migrateProductToMediaUri(dto: ProductRapidDTO, inDbList: List<Media>) {
        inDbList.forEach {
            mediaUriRepository.findByUri(it.uri ) ?: run {
                LOG.info("Migrating uri ${it.uri} for series ${dto.seriesUUID}")
                mediaUriRepository.save(MediaUri (
                    uri = it.uri,
                    filename = it.filename ?: it.uri,
                    oid = dto.seriesUUID!!,
                    created = it.created,
                    source = it.source,
                    updated = it.updated,
                    md5 = it.md5,
                    objectType = ObjectType.SERIES,
                    size = it.size,
                    sourceUri = it.sourceUri,
                    type = it.type
                ))
            }
        }
    }

    suspend fun migrateAgreementToMediaUri(dto: AgreementDTO, inDbList: List<Media>) {
        inDbList.forEach {
            mediaUriRepository.findByUri(it.uri) ?: run {
                LOG.info("Migrating uri ${it.uri} for agreement ${dto.id}")
                mediaUriRepository.save(MediaUri (
                    uri = it.uri,
                    filename = it.filename ?: it.uri,
                    oid = dto.id,
                    created = it.created,
                    source = it.source,
                    updated = it.updated,
                    md5 = it.md5,
                    objectType = ObjectType.AGREEMENT,
                    size = it.size,
                    sourceUri = it.sourceUri,
                    type = it.type
                ))
            }
        }
    }
}