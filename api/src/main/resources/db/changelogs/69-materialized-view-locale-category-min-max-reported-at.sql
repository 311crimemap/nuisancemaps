-- liquibase formatted sql

-- changeset root:1734240194723-1
CREATE MATERIALIZED VIEW locale_category_min_max_reported_at AS
    SELECT
        s.locale_id,
        'crime' AS category,
        min(d.reported_at) AS min_reported_at,
        max(d.reported_at) AS max_reported_at
    FROM
        data_crime AS d
        JOIN source AS s ON d.source_id = s.id
    GROUP BY
        s.locale_id
    UNION ALL
        SELECT
            s.locale_id,
            '311' AS category,
            min(d.reported_at) AS min_reported_at,
            max(d.reported_at) AS max_reported_at
        FROM
            data_311 AS d
            JOIN source AS s ON d.source_id = s.id
        GROUP BY
            s.locale_id
    ORDER BY
        locale_id, category;
