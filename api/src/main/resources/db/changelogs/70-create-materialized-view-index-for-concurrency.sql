-- liquibase formatted sql

-- changeset root:1734304401045-1
CREATE UNIQUE INDEX ON public.locale_category_min_max_reported_at (locale_id, category);

