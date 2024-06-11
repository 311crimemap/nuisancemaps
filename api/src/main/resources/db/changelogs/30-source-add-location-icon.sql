-- liquibase formatted sql

-- changeset root:1718049931093-1
ALTER TABLE source ADD icon_name VARCHAR(255);

-- changeset root:1718049931093-2
ALTER TABLE source ADD icon_unicode VARCHAR(255);

-- changeset root:1718049931093-3
ALTER TABLE source ADD location GEOMETRY;

