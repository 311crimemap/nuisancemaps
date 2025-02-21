-- liquibase formatted sql

-- changeset root:1740159783949-1
ALTER TABLE source ADD recurring BOOLEAN NOT NULL DEFAULT FALSE;

