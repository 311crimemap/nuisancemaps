-- liquibase formatted sql

-- changeset root:1712173377634-1
ALTER TABLE category DROP CONSTRAINT uc_categorytext_col;

-- changeset root:1712173377634-2
ALTER TABLE category ADD CONSTRAINT "UniqueTextAndLabel" UNIQUE (text, label);

