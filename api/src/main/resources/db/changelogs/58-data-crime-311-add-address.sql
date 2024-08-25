-- liquibase formatted sql

-- changeset root:1724618761414-1
ALTER TABLE data_311 ADD address VARCHAR(512);

-- changeset root:1724618761414-2
ALTER TABLE data_crime ADD address VARCHAR(512);

