-- liquibase formatted sql

-- changeset root:1731297798201-1
ALTER TABLE data_crime ALTER COLUMN id TYPE BIGINT;

-- changeset root:1731297798201-2
ALTER TABLE data_311 ALTER COLUMN id TYPE BIGINT;
