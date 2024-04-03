-- liquibase formatted sql

-- changeset root:1712171867664-1
ALTER TABLE category ADD CONSTRAINT UC_CATEGORYTEXT_COL UNIQUE (text);
