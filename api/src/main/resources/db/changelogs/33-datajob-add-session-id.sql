-- liquibase formatted sql

-- changeset root:1720836392175-1
ALTER TABLE data_job ADD session_id TIMESTAMP(6) WITHOUT TIME ZONE;

