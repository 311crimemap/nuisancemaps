# nuisancemaps

## Setup Fresh

* Create database, install postgis extension:

```
docker-compose run db bash
psql -U postgres

create database nuisancemaps;
\c nuisancemaps
create extension postgis;
create database nuisancemaps_test;
\c nuisancemaps_test
create extension postgis;
```

* Run database migrations

```
docker-compose run api ash  # yes 'ash'
./mvnw liquibase:update
./mvnw liquibase:update -P test -Dspring.profiles.active=test
```

* Disable archive mode

```
# db/archive.conf
archive_mode=off   # change
```

* (optional) OR db restore (if db archive available)

````
# 1. create stanza
docker-compose exec db
pgbackrest --stanza=311crimemap stanza-create

# 2. fetch archive
# shutdown any running pg instance
docker-compose run db bash
pgbackrest --stanza=311crimemap --type=immediate --delta \
    --target-action=promote --log-level-console=detail restore

# 3. restart pg instance in recovery mode
# uncomment docker-compose.yml ./run.sh mount
docker-compose up db
pgbackrest --stanza=311crimemap stanza-upgrade

# 4 restart pg instance
# comment  docker-compose.yml ./run.sh mount to disable recovery mode
docker-compose up db
````

### DataJob

#### Restart / Update Jobs

1. `curl localhost:8080/sources/<id>/updateNumRecords`: will fetch and update source to most recent counts (takes a while)

2. `docker-compose restart worker`: restarts worker will check record count and fetch if necessary.

#### Broken Jobs

`curl localhost:8080/datajobs/restart`: finds last non-complete (not QUEUED, not
COMPLETED) and resets them to queued to be re-fetched.

####  Queue

See `DataJobStatus.java` for states of fetch and processing jobs:

* `PENDING`: means input to database failed. Just reset to `QUEUED` and
  `checkDataJobQueue` task will re-attempt it. (We upsert entries.)

```
UPDATE data_job SET status='QUEUED' where id = 315;
```

* `FETCH_START`: dangling job here will just remain. The `createDailiyDataJobs`
  task will create redo this and create another `QUEUED` job the next day.

---

### Data Errors

#### List Errors

`select * from  data_error`: typically show missing text_category mappings.

Best to batch by 311/crime - often many duplicates of missing categories.

To create the missing TextCategory record:

1. Collect the missing `text` content per each crime/311 record

* Query for the incident to get text label: `curl 'https://data.austintexas.gov/resource/xwdj-i9he.json?sr_number=24-00123368`

2. Figure out the `label` number that corresponds to its category


* Category Labels: `curl localhost:8080/categories`
* Decide and assign the label: for each error category see if it matches a
  similar text category

    * Lookup similar `textcategories` with keyword (e.g.) "SBO": `curl localhost:8080/textcategories | jq '[.data[] | select(.text | contains("SBO")) | {text: .text, label: .category.label}]'`
    * browse errors all via type [ `sr_type_desc`, `crime_type` ]: `curl localhost:8080/dataerrors |jq '{content: .[].content, errorMsg: .[].errorMsg }'  | grep sr_type_desc _`

* Decide  category and label from above queries or manually

3. Build and Submit the record:

```
curl -H 'content-type:application/json' -X POST -d '[{"dataType": "crime", "text":"Animal bite rawr", "label": 0}]' localhost:8080/textcategories
```

4. Re-send / Re-process the data_job

* Collect unique urls for errors: `curl localhost:8080/dataerrors?limit=500 | jq '[.[].dataJob.url] | unique'`

* Use urls to query in data_job, e.g.:

```
select id from data_job where url in (
  'https://data.austintexas.gov/resource/fdj4-gpfu.json?$limit=10000&$offset=2460000&$order=incident_report_number&$select=',
  'https://data.austintexas.gov/resource/xwdj-i9he.json?$limit=10000&$offset=1800000&$order=sr_number&$select=',
  'https://data.austintexas.gov/resource/xwdj-i9he.json?$limit=10000&$offset=1830000&$order=sr_number&$select=',
  'https://data.austintexas.gov/resource/xwdj-i9he.json?$limit=10000&$offset=1840000&$order=sr_number&$select=',
  'https://data.austintexas.gov/resource/xwdj-i9he.json?$limit=10000&$offset=1860000&$order=sr_number&$select='
);

```

```
  id
------
 1469
 1479
 1481
 1466
 1474
...
```

* Use ids to update jobs:

`curl -H "content-type: application/json" -X PATCH -d '{"status":"QUEUED"}' localhost:8080/datajobs/1469`

`curl -H "content-type: application/json" -X PATCH -d '{"status":"QUEUED"}' localhost:8080/datajobs/1479`

... etc


* Delete errors: `delete from data_error;`

* Restart Worker: `docker-compose restart worker``



---

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
