CREATE TABLE IF NOT EXISTS media_v1 (
    uri VARCHAR(255) NOT NULL PRIMARY KEY,
    oid UUID NOT NULL,
    priority INTEGER NOT NULL DEFAULT 1,
    type VARCHAR(32) NOT NULL,
    text TEXT,
    status VARCHAR(32) NOT NULL,
    source VARCHAR(32) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX media_v1_updated_idx ON media_v1(updated);
CREATE INDEX media_v1_oid_idx ON media_v1(oid);
CREATE INDEX media_v1_status_idx ON media_v1(status);

