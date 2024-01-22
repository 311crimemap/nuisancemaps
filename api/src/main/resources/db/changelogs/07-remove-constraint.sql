-- liquibase formatted sql

-- changeset root:1705618434478-1
ALTER TABLE data_crime
DROP CONSTRAINT fk_source;
