-- liquibase formatted sql

-- changeset root:1706914955477-1
ALTER TABLE data_job ADD num_fetched INTEGER;

-- changeset root:1706914955477-2
ALTER TABLE data_job RENAME COLUMN num_results TO num_processed;
