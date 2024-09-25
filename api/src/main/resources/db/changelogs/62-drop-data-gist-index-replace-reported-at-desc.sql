-- liquibase formatted sql

-- changeset root:1727223032624-1
CREATE INDEX idx_reported_at_desc_data_311 ON data_311(reported_at DESC);

-- changeset root:1727223032624-2
CREATE INDEX idx_reported_at_desc_data_crime ON data_crime(reported_at DESC);

-- changeset root:1727223032624-3
DROP INDEX idx_reported_at_311;

-- changeset root:1727223032624-4
DROP INDEX idx_reported_at_crime

-- changeset root:1727223032624-5
DROP INDEX idx_point_data_311

-- changeset root:1727223032624-6
DROP INDEX idx_point_data_crime

