-- liquibase formatted sql

-- changeset root:1722292273574-1
ALTER TABLE source ADD data_process_type SMALLINT;

-- changeset root:1722292273574-5
ALTER TABLE source DROP COLUMN data_processing_type;

