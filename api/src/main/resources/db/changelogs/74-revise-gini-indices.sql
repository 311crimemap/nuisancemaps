-- liquibase formatted sql

-- changeset root:1776823299790-1
CREATE INDEX idx_point_gist_data_311 ON data_311 USING GIST(point);

-- changeset root:1776823299790-2
CREATE INDEX idx_point_gist_data_crime ON data_crime USING GIST(point);

-- changeset root:1776823299790-3
DROP INDEX idx_gist_point_reported_at_data_crime;

-- changeset root:1776823299790-4
DROP INDEX idx_gist_point_reported_at_data_311;

-- changeset root:1776823299790-5
ALTER TABLE data_crime ALTER COLUMN point SET STATISTICS 1000;

-- changeset root:1776823299790-6
ALTER TABLE data_crime ALTER COLUMN reported_at SET STATISTICS 1000;

-- changeset root:1776823299790-7
ALTER TABLE data_311 ALTER COLUMN point SET STATISTICS 1000;

-- changeset root:1776823299790-8
ALTER TABLE data_311 ALTER COLUMN reported_at SET STATISTICS 1000;

-- changeset root:1776823299790-9
ANALYZE data_crime;

-- changeset root:1776823299790-10
ANALYZE data_311;
