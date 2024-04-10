-- liquibase formatted sql

-- changeset root:1712698329921-1
ALTER TABLE source ADD CONSTRAINT uc_sourcesource_config_id_col UNIQUE (source_config_id);

