-- liquibase formatted sql

-- changeset root:1724019202976-1
ALTER TABLE data_job ALTER COLUMN url TYPE VARCHAR(1024) USING (url::VARCHAR(1024));

-- changeset root:1724019202976-2
ALTER TABLE source ALTER COLUMN url TYPE VARCHAR(1024) USING (url::VARCHAR(1024));

