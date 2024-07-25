-- liquibase formatted sql

-- changeset root:1721948283023-1
ALTER TABLE Source ALTER COLUMN locale_id SET NOT NULL;
