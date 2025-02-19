-- liquibase formatted sql

-- changeset root:1739999597924-1
ALTER TABLE data_311 ADD CONSTRAINT uc_data_311_source_id_report_num UNIQUE (source_id, report_num);

-- changeset root:1739999597924-2
ALTER TABLE data_crime ADD CONSTRAINT uc_data_crime_source_id_report_num UNIQUE (source_id, report_num);
