-- liquibase formatted sql

-- changeset root:1706055043019-1
ALTER TABLE source ADD source_config_entity VARCHAR(255);

-- changeset root:1706055043019-2
ALTER TABLE source ADD source_config_id INTEGER;

-- changeset root:1706055043019-3
CREATE INDEX source_config_entity_idx ON source(source_config_entity);
