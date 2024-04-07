-- liquibase formatted sql

-- changeset root:1712355445685-1
ALTER TABLE data_311 ADD category_id INTEGER;

-- changeset root:1712355445685-2
ALTER TABLE data_crime ADD category_id INTEGER;

-- changeset root:1712355445685-3
ALTER TABLE data_crime ADD CONSTRAINT "data_crime_category_id_fkey" FOREIGN KEY (category_id) REFERENCES category (id);

-- changeset root:1712355445685-4
ALTER TABLE data_311 ADD CONSTRAINT "data_311_category_id_fkey" FOREIGN KEY (category_id) REFERENCES category (id);
