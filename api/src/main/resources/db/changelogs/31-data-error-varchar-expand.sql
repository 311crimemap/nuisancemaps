-- liquibase formatted sql

-- changeset root:1719592647031-1
ALTER TABLE data_error ALTER COLUMN content TYPE VARCHAR(4096) USING (content::VARCHAR(4096));

-- changeset root:1719592647031-2
ALTER TABLE data_error ALTER COLUMN error_msg TYPE VARCHAR(4096) USING (error_msg::VARCHAR(4096));

