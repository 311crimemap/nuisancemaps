-- liquibase formatted sql

-- changeset root:1717096081273-1
ALTER TABLE category ADD icon_unicode VARCHAR(255);

-- changeset root:1717096081273-5
ALTER TABLE category DROP COLUMN icon_filename;

