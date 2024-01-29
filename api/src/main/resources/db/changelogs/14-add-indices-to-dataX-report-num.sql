-- liquibase formatted sql

-- changeset root:1706558006780-1
CREATE INDEX idx_report_num_data_crime ON data_crime (report_num);
CREATE INDEX idx_report_num_data_311 ON data_311 (report_num);
