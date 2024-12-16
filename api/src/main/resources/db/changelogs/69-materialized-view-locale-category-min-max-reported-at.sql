-- liquibase formatted sql

-- changeset root:1734240194723-1
CREATE MATERIALIZED VIEW locale_category_min_max_reported_at AS
    WITH
        data_crime_min_max
            AS (
                SELECT
                    s.locale_id,
                    'crime' AS category,
                    min(d.reported_at) AS min_reported_at,
                    max(d.reported_at) AS max_reported_at,
                    count(d.id) AS count
                FROM
                    data_crime AS d
                    JOIN source AS s ON d.source_id = s.id
                GROUP BY
                    s.locale_id
            ),
        data_311_min_max
            AS (
                SELECT
                    s.locale_id,
                    '311' AS category,
                    min(d.reported_at) AS min_reported_at,
                    max(d.reported_at) AS max_reported_at,
                    count(d.id) AS count
                FROM
                    data_311 AS d
                    JOIN source AS s ON d.source_id = s.id
                GROUP BY
                    s.locale_id
            ),
        max_data_job_dates
            AS (
                SELECT
                    s.locale_id,
                    s.category,
                    max(dj.updated_at) AS max_updated_at
                FROM
                    data_job AS dj
                    JOIN source AS s ON dj.source_id = s.id
                GROUP BY
                    s.locale_id, s.category
            )
    SELECT
        dcrime.locale_id,
        dcrime.category,
        dcrime.min_reported_at,
        dcrime.max_reported_at,
        dcrime.count,
        dj.max_updated_at AS dj_max_updated_at
    FROM
        data_crime_min_max AS dcrime
        LEFT JOIN max_data_job_dates AS dj ON
                dcrime.locale_id = dj.locale_id
                AND dcrime.category = dj.category
    UNION ALL
        SELECT
            d311.locale_id,
            d311.category,
            d311.min_reported_at,
            d311.max_reported_at,
            d311.count,
            dj.max_updated_at AS dj_max_updated_at
        FROM
            data_311_min_max AS d311
            LEFT JOIN max_data_job_dates AS dj ON
                    d311.locale_id = dj.locale_id
                    AND d311.category = dj.category
    ORDER BY
        locale_id, category WITH DATA;
