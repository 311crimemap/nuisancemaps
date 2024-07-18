-- liquibase formatted sql

-- changeset root:1721343747171-1
ALTER TABLE mapping ADD description_field VARCHAR(255);

-- changeset root:1721343747171-2
ALTER TABLE mapping ADD description_parsing_strategy VARCHAR(255);

-- changeset root:1721343747171-3
ALTER TABLE mapping ADD description_pointer VARCHAR(255);

-- changeset root:1721343747171-5
ALTER TABLE mapping ADD location_field VARCHAR(255);

-- changeset root:1721343747171-6
ALTER TABLE mapping ADD location_parsing_strategy VARCHAR(255);

-- changeset root:1721343747171-7
ALTER TABLE mapping ADD location_pointer VARCHAR(255);

-- changeset root:1721343747171-8
ALTER TABLE mapping ADD longitude_field VARCHAR(255);

-- changeset root:1721343747171-9
ALTER TABLE mapping ADD longitude_parsing_strategy VARCHAR(255);

-- changeset root:1721343747171-10
ALTER TABLE mapping ADD longitude_pointer VARCHAR(255);

-- changeset root:1721343747171-13
ALTER TABLE mapping ADD report_category_field VARCHAR(255);

-- changeset root:1721343747171-14
ALTER TABLE mapping ADD report_category_parsing_strategy VARCHAR(255);

-- changeset root:1721343747171-15
ALTER TABLE mapping ADD report_category_pointer VARCHAR(255);

-- changeset root:1721343747171-16
ALTER TABLE mapping ADD report_num_field VARCHAR(255);

-- changeset root:1721343747171-17
ALTER TABLE mapping ADD report_num_parsing_strategy VARCHAR(255);

-- changeset root:1721343747171-18
ALTER TABLE mapping ADD report_num_pointer VARCHAR(255);

-- changeset root:1721343747171-19
ALTER TABLE mapping ADD reported_at2_field VARCHAR(255);

-- changeset root:1721343747171-20
ALTER TABLE mapping ADD reported_at2_parsing_strategy VARCHAR(255);

-- changeset root:1721343747171-21
ALTER TABLE mapping ADD reported_at2_pointer VARCHAR(255);

-- changeset root:1721343747171-22
ALTER TABLE mapping ADD reported_at_field VARCHAR(255);

-- changeset root:1721343747171-23
ALTER TABLE mapping ADD reported_at_parsing_strategy VARCHAR(255);

-- changeset root:1721343747171-24
ALTER TABLE mapping ADD reported_at_pointer VARCHAR(255);

-- changeset root:1721343747171-28
DROP TABLE test;

-- changeset root:1721343747171-29
ALTER TABLE mapping DROP COLUMN description;

-- changeset root:1721343747171-30
ALTER TABLE mapping DROP COLUMN latitude;

-- changeset root:1721343747171-31
ALTER TABLE mapping DROP COLUMN location;

-- changeset root:1721343747171-32
ALTER TABLE mapping DROP COLUMN longitude;

-- changeset root:1721343747171-33
ALTER TABLE mapping DROP COLUMN report_category;

-- changeset root:1721343747171-34
ALTER TABLE mapping DROP COLUMN report_num;

-- changeset root:1721343747171-35
ALTER TABLE mapping DROP COLUMN reported_at;

-- changeset root:1721343747171-36
ALTER TABLE mapping DROP COLUMN reported_at2;

-- changeset root:1721343747171-37
DROP SEQUENCE "Test_SEQ" CASCADE;

