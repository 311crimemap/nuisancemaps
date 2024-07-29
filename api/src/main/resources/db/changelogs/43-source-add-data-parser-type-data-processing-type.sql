-- liquibase formatted sql

-- changeset root:1722285773303-1
ALTER TABLE source ADD data_parser_type SMALLINT;

-- changeset root:1722285773303-2
ALTER TABLE source ADD data_processing_type SMALLINT;
