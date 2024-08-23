-- liquibase formatted sql

-- changeset root:1724357134444-1
ALTER TABLE geocode ADD CONSTRAINT "uc_geocode_source_id_address_col" UNIQUE (source_id, address);

-- changeset root:1724357134444-2
CREATE INDEX idx_address ON geocode(address);
