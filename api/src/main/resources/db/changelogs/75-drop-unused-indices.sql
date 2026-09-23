-- liquibase formatted sql

-- changeset root:1784529449406-1
-- redundant with uc_data_311_source_id_report_num (source_id, report_num) leading column
DROP INDEX IF EXISTS idx_source_id_data_311;

-- changeset root:1784529449406-2
-- redundant with uc_data_crime_source_id_report_num (source_id, report_num) leading column
DROP INDEX IF EXISTS idx_source_id_data_crime;

-- changeset root:1784529449406-3
-- duplicate of unique index uc_geocode_source_id_address_col (source_id, address)
DROP INDEX IF EXISTS idx_source_address;

-- changeset root:1784529449406-4
-- unused: no queries filter data_job by session_id alone
DROP INDEX IF EXISTS idx_session_id_data_job;
