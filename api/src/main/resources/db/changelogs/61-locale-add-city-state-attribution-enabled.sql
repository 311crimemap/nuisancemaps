-- liquibase formatted sql

-- changeset root:1724941467309-1
ALTER TABLE locale ADD city VARCHAR(255);

-- changeset root:1724941467309-2
ALTER TABLE locale ADD state VARCHAR(255);

-- changeset root:1724941467309-3
ALTER TABLE locale ADD attribution VARCHAR(4096);

-- changeset root:1724941467309-4
ALTER TABLE locale ADD enabled BOOLEAN NOT NULL DEFAULT FALSE;
