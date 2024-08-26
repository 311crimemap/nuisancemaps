-- liquibase formatted sql

-- changeset root:1724619228523-1
ALTER TABLE mapping ADD address_field VARCHAR(255);

-- changeset root:1724619228523-2
ALTER TABLE mapping ADD address_parsing_strategy VARCHAR(255);

-- changeset root:1724619228523-3
ALTER TABLE mapping ADD address_pointer VARCHAR(255);

