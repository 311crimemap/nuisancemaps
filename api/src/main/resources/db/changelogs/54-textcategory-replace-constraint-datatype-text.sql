-- liquibase formatted sql

-- changeset root:1724451711427-1
ALTER TABLE text_category DROP CONSTRAINT "UniqueDataTypeAndTextAndLabel";

-- changeset root:1724451711427-2
ALTER TABLE text_category ADD CONSTRAINT "UniqueDataTypeAndText" UNIQUE (data_type, text);
