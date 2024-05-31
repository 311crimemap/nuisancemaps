-- liquibase formatted sql

-- changeset root:1715480265835-1
ALTER TABLE category ADD icon_filename VARCHAR(255);

-- changeset root:1715480265835-2
ALTER TABLE category ADD icon_name VARCHAR(255);

