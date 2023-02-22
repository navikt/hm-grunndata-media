package no.nav.hm.grunndata.media.model

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect

@JdbcRepository(dialect = Dialect.POSTGRES)
interface MediaRepository {
}
