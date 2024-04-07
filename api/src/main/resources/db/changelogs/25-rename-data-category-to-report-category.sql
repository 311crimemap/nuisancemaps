-- liquibase formatted sql

-- changeset root:1712357737440-1
ALTER TABLE data_311 RENAME COLUMN category TO report_category;

-- changeset root:1712357737440-2
ALTER TABLE data_crime RENAME COLUMN category TO report_category;

