-- liquibase formatted sql

-- changeset root:1721348616566-1
ALTER TABLE mapping ADD description_field VARCHAR(255);

-- changeset root:1721348616566-2
ALTER TABLE mapping ADD description_parsing_strategy VARCHAR(255);

-- changeset root:1721348616566-3
ALTER TABLE mapping ADD description_pointer VARCHAR(255);

-- changeset root:1721348616566-7
ALTER TABLE mapping DROP COLUMN description;

