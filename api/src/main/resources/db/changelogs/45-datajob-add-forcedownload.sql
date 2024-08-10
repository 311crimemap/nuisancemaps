-- liquibase formatted sql

-- changeset root:1723329684028-1
ALTER TABLE data_job ADD force_download BOOLEAN NOT NULL DEFAULT false;

