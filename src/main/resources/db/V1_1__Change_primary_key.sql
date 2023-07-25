ALTER TABLE media_v1 ADD COLUMN id UUID NOT NULL DEFAULT gen_random_uuid();
CREATE UNIQUE INDEX media_v1_oid_uri_idx ON media_v1(oid,uri);
ALTER TABLE media_v1 DROP CONSTRAINT media_v1_pkey;
ALTER TABLE media_v1 ADD PRIMARY KEY (id);