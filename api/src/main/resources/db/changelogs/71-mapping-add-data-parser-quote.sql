-- liquibase formatted sql

-- changeset root:1735433041231-1
ALTER TABLE mapping ADD data_parser_quote VARCHAR(255) DEFAULT '"';
