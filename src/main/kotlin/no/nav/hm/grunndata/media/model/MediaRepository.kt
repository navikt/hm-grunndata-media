package no.nav.hm.grunndata.media.model

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface MediaRepository: CoroutinePageableCrudRepository<Media, String>, CoroutineJpaSpecificationExecutor<String> {

    suspend fun findByUri(uri:String): Media?

    suspend fun findByOid(oid:UUID): List<Media>


}
