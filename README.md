# nuisancemaps

### DataJob Queue

See `DataJobStatus.java` for states of fetch and processing jobs:

* `PENDING`: means input to database failed. Just reset to `QUEUED` and
  `checkDataJobQueue` task will re-attempt it. (We upsert entries.)

```
UPDATE data_job SET status='QUEUED' where id = 315;
```

* `FETCH_START`: dangling job here will just remain. The `createDailiyDataJobs`
  task will create redo this and create another `QUEUED` job the next day.


### Spatial Queries

* "Geometries" in GeoJSON/WKB format are (long, lat) - so they are "reversed", in a sense.

##### Spatial Index

JPA doesn't have an annotation property to explicitly declare the type, so make
sure to manually do so in liquibase migration.

`CREATE INDEX idx_point_data_311 ON data_311 USING GIST (point);`

##### Example Queries

* `ST_DWithin(geometry A, geometry B, distance)`: checks whether two geometries (A and B) are within a specified distance of each other.
* `ST_MakePoint(x, y)`
* `ST_MakeEnvelope(minx, miny, maxx, maxy, srid)`: creates a rectangular bounding box (envelope).
* `ST_Buffer(geometry, distance)`: creates new geometry of specified distance from original geometry
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

`select id, category, ST_AsText(ST_GeomFromEWKB(decode(point, 'hex'))) from data_crime limit 20;`


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

`explain` shows the initial bbox query via `ST_MakeEnvelope` better leverages the
index vs `ST_Within` which has to scan. The sort (order by) is expensive:

* `Index Scan using idx_point_data_crime on data_crime  (cost=0.41..235.69 rows=14 width=649)`
* `Bitmap Heap Scan on data_crime  (cost=16.18..7940.64 rows=486 width=649)`
