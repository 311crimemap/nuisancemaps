-- liquibase formatted sql

-- changeset root:1721345558500-2
ALTER TABLE mapping ADD latitude_field VARCHAR(255);

-- changeset root:1721345558500-3
ALTER TABLE mapping ADD latitude_parsing_strategy VARCHAR(255);

-- changeset root:1721345558500-4
ALTER TABLE mapping ADD latitude_pointer VARCHAR(255);

-- changeset root:1721345558500-10
ALTER TABLE mapping DROP COLUMN description_field;

-- changeset root:1721345558500-11
ALTER TABLE mapping DROP COLUMN description_parsing_strategy;

-- changeset root:1721345558500-12
ALTER TABLE mapping DROP COLUMN description_pointer;

