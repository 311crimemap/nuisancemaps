-- liquibase formatted sql

-- changeset root:1732311840665-1
ALTER TABLE data_url_cache DROP CONSTRAINT UC_DATA_URL_CACHEURL_COL;

-- changeset root:1732311840665-2
ALTER TABLE data_url_cache ADD CONSTRAINT UC_DATA_URL_CACHEURL_COL UNIQUE (url);

-- changeset root:1732311840665-3
ALTER TABLE data_url_cache ADD created_at TIMESTAMP(6) WITHOUT TIME ZONE;

-- changeset root:1732311840665-4
ALTER TABLE data_url_cache ADD updated_at TIMESTAMP(6) WITHOUT TIME ZONE;

