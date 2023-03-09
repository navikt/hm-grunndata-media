CREATE TABLE IF NOT EXISTS media_v1 (
    uri VARCHAR(2048) NOT NULL PRIMARY KEY,
    source_uri VARCHAR(2048) NOT NULL,
    oid UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    size BIGINT NOT NULL,
    md5 VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    source VARCHAR(32) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX media_v1_updated_idx ON media_v1(updated);
CREATE INDEX media_v1_oid_idx ON media_v1(oid);
CREATE INDEX media_v1_status_idx ON media_v1(status);
CREATE INDEX media_v1_source_uri_idx on media_v1(source_uri);

