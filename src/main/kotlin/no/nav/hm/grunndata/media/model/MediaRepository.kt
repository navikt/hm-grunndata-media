package no.nav.hm.grunndata.media.model

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository
import java.time.LocalDateTime
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface MediaRepository : CoroutinePageableCrudRepository<Media, UUID>,
    CoroutineJpaSpecificationExecutor<UUID> {

    suspend fun findByOid(oid: UUID): List<Media>

    suspend fun findOneByUri(uri: String): Media?

    suspend fun findOneByUriAndStatus(uri: String, status: MediaStatus): Media?

    suspend fun findByOidAndUri(oid: UUID, uri: String): Media?

    suspend fun findByUri(uri: String): List<Media>

    //suspend fun findByStatusAndUpdatedBefore(status: MediaStatus, updated: LocalDateTime): List<Media>

    suspend fun findByStatusInListAndUpdatedBefore(statusList: List<MediaStatus>, updated: LocalDateTime): List<Media>
    suspend fun findDistinctUriByStatus(status: MediaStatus): List<String>

}
