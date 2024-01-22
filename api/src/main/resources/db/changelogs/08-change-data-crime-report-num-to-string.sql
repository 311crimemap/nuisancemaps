-- liquibase formatted sql

-- changeset root:1705624990684-1
ALTER TABLE data_crime
ALTER COLUMN report_num TYPE VARCHAR(255);
