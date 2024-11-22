-- liquibase formatted sql

-- changeset root:1732310261651-3
ALTER TABLE data_url_cache DROP CONSTRAINT uc_data_url_cachehash_code_col;

-- changeset root:1732310261651-7
ALTER TABLE data_url_cache DROP COLUMN hash_code;

-- changeset root:1732310261651-1
ALTER TABLE data_url_cache DROP CONSTRAINT UC_DATA_URL_CACHEURL_COL;

-- changeset root:1732310261651-2
ALTER TABLE data_url_cache ADD CONSTRAINT UC_DATA_URL_CACHEURL_COL UNIQUE (url);

