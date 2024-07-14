-- liquibase formatted sql

-- changeset root:1720993371708-1
CREATE INDEX idx_session_id_data_job ON data_job(session_id);

-- changeset root:1720993371708-2
CREATE INDEX idx_source_id_data_job ON data_job(source_id);
