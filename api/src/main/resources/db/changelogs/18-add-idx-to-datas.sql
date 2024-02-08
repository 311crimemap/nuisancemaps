-- liquibase formatted sql

-- changeset root:1707424205441-1
CREATE INDEX idx_source_id_data_311 ON data_311(source_id);

-- changeset root:1707424205441-2
CREATE INDEX idx_source_id_data_crime ON data_crime(source_id);

