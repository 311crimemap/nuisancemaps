# Database Query and Index Optimization Examples




#### Query Improvement



Tried a number of indices in an attempt to improve query speed.

Current fastest implementation:

1. Set a GIST index
2. Increase the statistics on `point` and `reported_at` columns

Spatial queries need larger than default (100) statistics.

```
ALTER TABLE data_crime ALTER COLUMN point SET STATISTICS 1000;
ALTER TABLE
ALTER TABLE data_crime ALTER COLUMN reported_at SET STATISTICS 1000;
ALTER TABLE
ANALYZE data_crime;

ALTER TABLE data_311 ALTER COLUMN point SET STATISTICS 1000;
ALTER TABLE
ALTER TABLE data_311 ALTER COLUMN reported_at SET STATISTICS 1000;
ALTER TABLE
ANALYZE data_311;
```

#### Exploration Notes

Non-indexed query took almost 5 seconds. Intuitively, it seemed adding indices on
the queried data would be beneficial, namely on the `point` gis data, and on
`reported_at DESC` field, as the query using dates were also sorted.

```
--- initial index implementation (not currently used, but present to describe intuition)

--- CREATE INDEX idx_point_reported_at_GIST_311 ON data_311 USING GIST (point) INCLUDE (reported_at);
--- CREATE INDEX idx_point_reported_at_desc_311 ON data_311 (reported_at DESC, point);


--- current implementation

CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE INDEX idx_gist_point_reported_at_data_311 ON data_311(point, reported_at);
CREATE INDEX idx_gist_point_reported_at_data_crime ON data_crime(point, reported_at);
```

However, postgis can't create compound index on `reported_at DESC`, just
`reported_at` - which is important. So the thought is to simply make two
indices:

1. GIST on point with an INCLUDE to associate the point with reported_at
2. B-tree compound index on reported_at DESC, point - this isn't spatial.

The hope is these two can be used in combination to query on geo and date.

#### Reality

Unfortunately, the query planner will create different query plans depending on
the location, often sidestepping the entire date index, or using a pre-existing
GIST index without the INCLUDE.

The most costly step is date sorting, but often the reported_at index would be
ignored in favor of a GIST, and then sequentially scanned to filter by date.

Note: It's acutally more nuanced. When zoomed out, the bounding box is fairly
large geographic area, so almost all points are spatially valid - which makes
sorting by date the expensive step. However on narrow geographic ranges, the
spatial query needs to filter out the same sorted date candidates, so the
spatial query becomes a bottleneck.

To strike a balance, used btree_gist extension to allow compound index using GIST.

(Unfortunately, still cannot use ASC/DESC)

#### Current Implementation

Turns out that a single index on `reported_at DESC` works well for dense sets
(e.g. NYC, slower on Dallas or Austin), where there's a limited geographic area.

However, once a narrower lat/lng range is selected, the lack of any spatial
query becomes severely noticeable, with query times running from 2 - 5 seconds.

This is likely because as the geographic range narrows, every point in valid
date range needs to be scanned vs taking advantage of a spatial index.

Found a `btree_gist` index that enabled a compound GIST index: `(point,
reported_at)` - so can provide some spatial query support.

On average, all queries are slightly slower but still somewhat (I guess, still
slow) acceptable. However, it drastically speeds up any zoomed in areas with
narrower lat/lng bounding boxes. On the whole it's an improvement.

Also still allows the query planner to use the singular `reported_at DESC` index
on it's own - so there are still some very fast results. (see below, previous
implementation.)


#### Previous Implementation

The most reliable way to get a reasonable query plan - longest < 400ms, with
internal database memoization, was to:

1. drop all GIST indices
2. create a single `reported_at DESC` index.

Yes actually removing indices resulting in 10x speed up because it forces the
query planner to use the `reported_at DESC` index all the time (which is the
bottleneck).


##### Secondary Exploration

Thought experiment: if it's more about forcing the query planner to sort by
date, what if we move the `point` data to it's own table; `data_311_spatial`,
and make an spatial index only on that data. This way `data_311` could be sorted
by date, `data_311_spatial` by GIST, and we could leverage two indices.

```
# slow ~ 8s-11000s

explain analyze SELECT s.*, cat.id as cat_id, cat.data_type, cat.text, cat.label, cat.parent_id
FROM data_311_spatial s
JOIN data_311 dc ON s.data_311_id = dc.id
JOIN category cat ON dc.category_id = cat.id
 WHERE dc.reported_at BETWEEN '2024-06-01' AND '2024-09-22'
   AND ST_Within(s.point, ST_MakeEnvelope(-97.9, 30.1, -97.5, 30.3, 4326)::geometry)
ORDER BY dc.reported_at DESC LIMIT 10000;
```

The reality was it was not any faster than the slowest original attempt with
GINI indices. The JOIN's lent to expensive scans, overwhelming any benefit of
separation.


#### Query Examples:

```
# austin 400ms
explain analyze SELECT dc.*, cat.id as cat_id, cat.data_type, cat.text, cat.label, cat.parent_id
FROM data_311 dc
JOIN category cat ON dc.category_id = cat.id
WHERE reported_at BETWEEN '2024-06-01' AND '2024-09-22' AND ST_Within(point, ST_MakeEnvelope(-97.9, 30.1, -97.5, 30.3, 4326)::geometry)
ORDER BY reported_at DESC LIMIT 10000;

# nyc 75ms
explain analyze SELECT dc.*, cat.id as cat_id, cat.data_type, cat.text, cat.label, cat.parent_id
FROM data_311 dc
JOIN category cat ON dc.category_id = cat.id
WHERE reported_at BETWEEN '2024-06-01' AND '2024-09-22' AND ST_Within(point, ST_MakeEnvelope(-74.2, 40.6, -73.8, 40.9, 4326)::geometry)
ORDER BY reported_at DESC LIMIT 10000;

# chicago 130ms
explain analyze SELECT dc.*, cat.id as cat_id, cat.data_type, cat.text, cat.label, cat.parent_id
FROM data_311 dc
JOIN category cat ON dc.category_id = cat.id
WHERE reported_at BETWEEN '2024-04-01' AND '2024-09-22' AND ST_Within(point, ST_MakeEnvelope(-87.8,41.8,-87.4,42,4326)::geometry)
ORDER BY reported_at DESC LIMIT 10000;

# dallas 170ms
explain analyze SELECT dc.*, cat.id as cat_id, cat.data_type, cat.text, cat.label, cat.parent_id
FROM data_311 dc
JOIN category cat ON dc.category_id = cat.id
WHERE reported_at BETWEEN '2024-06-01' AND '2024-09-22' AND ST_Within(point, ST_MakeEnvelope(-97,32.7,-96.6,32.9,4326)::geometry)
ORDER BY reported_at DESC LIMIT 10000;
```


#### Helpful queries / commands

* show index usage: `select * from pg_stat_user_indexes where relname = 'data_311';`

* turn off scanning types to force different index usage:

```
SET enable_indexscan = OFF;
SET enable_bitmapscan = OFF;
SET enable_seqscan = OFF;
```

* "analyze" table: `ANALYZE VERBOSE data_311;`

---

### Spatial Queries

* "Geometries" in GeoJSON/WKB format are (long, lat) - so they are "reversed",
  in a sense.

* NB: with larger tables, spatial queries become a bottleneck. For optimal
  query, need to induce a query plan that runs gis queries on a small subset;
  e.g. filter on date, etc. first.


##### Spatial Index

JPA doesn't have an annotation property to explicitly declare the type, so make
sure to manually do so in liquibase migration.

`CREATE INDEX idx_point_data_311 ON data_311 USING GIST (point);`

##### Example Queries

* `ST_DWithin(geometry A, geometry B, distance)`: checks whether two geometries
  (A and B) are within a specified distance of each other.
* `ST_MakePoint(x, y)`
* `ST_MakeEnvelope(minx, miny, maxx, maxy, srid)`: creates a rectangular
  bounding box (envelope).
* `ST_Buffer(geometry, distance)`: creates new geometry of specified distance
  from original geometry
* `::geography`: casting from geometry to the geography type


Current: All points within query between start and end dates:

This is relatively fast
```
SELECT dc.*, cat.id as cat_id, cat.data_type, cat.text, cat.label, cat.parent_id FROM data_crime dc
JOIN category cat ON dc.category_id = cat.id WHERE
ST_Within( point, ST_MakeEnvelope(-97.83187224, 30.2776401, -97.5973828, 30.3056545, 4326 )::geometry)
AND reported_at BETWEEN '2024-01-01' AND '2024-06-07';
```

See `Point` data from hex to text:

`select id, category, ST_AsText(ST_GeomFromEWKB(decode(point, 'hex'))) from
data_crime limit 20;`


50 recent crimes within 1 mile query:

```
SELECT id, report_num, category, location, reported_at
FROM data_crime
WHERE ST_DWithin(data_crime.point::geography, ST_MakePoint(-97.7171, 30.2944)::geography, 1 * 1609.34) order by reported_at desc limit 50;

```


All crimes within 1 mile query - notice this gets really slow.

```
SELECT id, report_num, category, location, reported_at
FROM data_crime
WHERE ST_DWithin(data_crime.point::geography, ST_MakePoint(-97.7171, 30.2944)::geography, 1 * 1609.34);
```


Key is to create an initial bounding box query as a filter within 1 mile.

```
SELECT *
FROM data_crime
WHERE point && ST_MakeEnvelope(
    -97.7171 - 1.0 / (cos(radians(30.2944)) * 69),
    30.2944 - 1.0 / 69,
    -97.7171 + 1.0 / (cos(radians(30.2944)) * 69),
    30.2944 + 1.0 / 69,
    4326
)
```


And then add the within query:

```
SELECT *
FROM data_crime
WHERE point && ST_MakeEnvelope(
    -97.7171 - 1.0 / (cos(radians(30.2944)) * 69),
    30.2944 - 1.0 / 69,
    -97.7171 + 1.0 / (cos(radians(30.2944)) * 69),
    30.2944 + 1.0 / 69,
    4326
    ) AND ST_Within(point, ST_Buffer(ST_MakePoint(-97.7171, 30.2944)::geography, 1*1609.34)::geometry) order by reported_at desc;
```


Or for a radius:

```
SELECT *
FROM data_crime
WHERE ST_Within(point, ST_Buffer(ST_MakePoint(-97.7171, 30.2944)::geography, 1*1609.34)::geometry) order by reported_at desc;

```

`explain` shows the initial bbox query via `ST_MakeEnvelope` better leverages
the index vs `ST_Within` which has to scan. The sort (order by) is expensive:

* `Index Scan using idx_point_data_crime on data_crime (cost=0.41..235.69
  rows=14 width=649)`
* `Bitmap Heap Scan on data_crime (cost=16.18..7940.64 rows=486 width=649)`
