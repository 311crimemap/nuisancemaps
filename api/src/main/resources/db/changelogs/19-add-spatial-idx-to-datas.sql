-- liquibase formatted sql

-- changeset root:1707860547122-1
CREATE INDEX idx_point_data_311 ON data_311 USING GIST (point);

-- changeset root:1707860547122-2
CREATE INDEX idx_point_data_crime ON data_crime USING GIST (point);

-- changeset root:1707860547122-3
CREATE INDEX idx_reported_at_311 ON data_311(reported_at);

-- changeset root:1707860547122-4
CREATE INDEX idx_reported_at_crime ON data_crime(reported_at);
