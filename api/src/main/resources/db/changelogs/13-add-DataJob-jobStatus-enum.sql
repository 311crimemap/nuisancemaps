-- liquibase formatted sql

-- changeset root:1706558006777-1
UPDATE data_job
SET status = 'QUEUED' WHERE status = 'queued';

UPDATE data_job
SET status = 'COMPLETED' WHERE status = 'completed';
