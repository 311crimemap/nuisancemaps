-- liquibase formatted sql

-- NB: these indices take a long time to build on a populated database

-- changeset root:1727301364414-1
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- changeset root:1727301364414-2
CREATE INDEX idx_gist_point_reported_at_data_311 ON data_311(point, reported_at);

-- changeset root:1727301364414-3
CREATE INDEX idx_gist_point_reported_at_data_crime ON data_crime(point, reported_at);
