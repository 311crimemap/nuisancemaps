-- liquibase formatted sql

-- changeset root:1721943481031-1
ALTER TABLE Source
  ALTER COLUMN locale_id TYPE INT USING locale_id::INT;
