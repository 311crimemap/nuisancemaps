-- liquibase formatted sql

-- changeset root:1734054484402-1
ALTER TABLE mapping ADD data_parser_delimeter VARCHAR(255) DEFAULT ',';

-- changeset root:1734054484402-2
ALTER TABLE mapping ADD data_parser_num_skip INTEGER DEFAULT 0;
