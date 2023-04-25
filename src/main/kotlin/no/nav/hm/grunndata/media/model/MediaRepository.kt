package no.nav.hm.grunndata.media.model

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository
import java.time.LocalDateTime
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface MediaRepository : CoroutinePageableCrudRepository<Media, MediaId>,
    CoroutineJpaSpecificationExecutor<MediaId> {

    suspend fun findByMediaIdOid(oid: UUID): List<Media>

    suspend fun findOneByMediaIdUri(uri: String): Media?

    suspend fun findOneByMediaIdUriAndStatus(uri: String, status: MediaStatus): Media?

    suspend fun findByMediaIdUri(uri: String): List<Media>

    suspend fun findByStatusAndUpdatedBefore(status: MediaStatus, updated: LocalDateTime): List<Media>

    suspend fun findDistinctMediaIdUriByStatus(status: MediaStatus): List<String>

}
