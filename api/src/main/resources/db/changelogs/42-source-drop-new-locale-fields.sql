-- liquibase formatted sql

-- changeset root:1722025149160-4
ALTER TABLE source DROP COLUMN icon_name;

-- changeset root:1722025149160-5
ALTER TABLE source DROP COLUMN icon_unicode;

-- changeset root:1722025149160-6
ALTER TABLE source DROP COLUMN location;

-- changeset root:1722025149160-7
ALTER TABLE source DROP COLUMN source_config_entity;

-- changeset root:1722025149160-8
ALTER TABLE source DROP COLUMN source_config_notes;

