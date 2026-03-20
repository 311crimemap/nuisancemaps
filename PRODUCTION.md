# Production Operations

## Build / Deploy STeps

### Web

1. verify `npm run compile` passes (typescript tsc check - will stop deploy on
   Cloudflare pages)

2. merge `master` in `deploy/staging`, `deploy/production` branch, Cloudflare
   Pages takes over build

### API

1. Setup ECR:

`aws ecr get-login-password --region $AWS_REGION --profile $AWS_PROFILE | \
docker login --username AWS --password-stdin $IMAGE_REPO`

2. Build api and worker container (fat jar) for deploy (not in container) - need
   IMAGE_REPO for build variable:

`./mvnw spring-boot:build-image -Dmaven.test.skip=true -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher -D$(grep IMAGE_REPO ../.env)`

3. `docker push $IMAGE_REPO/311crimemap/api:<TAG>`

4. Replace containers:

* `kubectl rollout restart deployment/spring-worker`
* `kubectl rollout restart deployment/spring-api`


## Sources

* To build list of source ids for daily update:
  * `curl -H 'X-API-KEY: <KEY>' -H 'content-type:application/json'  api.311crimemap.com/sources | jq`
  * NB: exclude large `dataProcessType: FILE` archives and use JSON subset by report
    date for city.

* To create a new source, look at
  `api/src/main/resources/data/source_config.json` to find an example per type -
  typically a `BASE` type starter and then an `OPENDATADATE` type for daily update.

* Add to source_config.json, and submit individual source id:

```
# add new source to locale

curl -X POST -H 'content-type: application/json' -H 'X-API-KEY: <KEY>' \
-d @source.json localhost:8080/locales/{locale_id}/sources
```

Job Restarts within past day: `curl -H "X-API-KEY: $ADMIN_API_KEY" api.311crimemap.com/datajobs/restart`

### Daily Runs: production-blue ids


| locale.id | Source                 | type  | source.id | locale | init | submit  freq | startReportedAt | Notes |
|-----------|------------------------|-------|-----------|--------|------|--------------|-----------------|-------|
| 1         | Austin                 | Crime | 2         | 1      | x    | -            | 2024-12-01      |       |
| 1         | Austin                 | 311   | 35        | 1      | x    |              | 2024-09-01      |       |
| 2         | Dallas                 | Crime | 36        | 2      | x    |              | 2024-09-01      |       |
| 2         | Dallas t-90 days       | 311   | 211       | 2      | x    |              | 2024-11-22      |       |
| 3         | Chicago                | Crime | 38        | 3      | x    |              | 2024-09-01      |       |
| 3         | Chicago                | 311   | 34        | 3      | x    |              |                 |       |
| 4         | NYC (YTD)              | crime | 186       | 4      | x    | quarterly    |                 |       |
| 4         | NYC                    | 311   | 32        | 4      | x    |              | 2024-09-01      |       |
| 5         | SF                     | Crime | 39        | 5      | x    |              | 2024-09-01      |       |
| 5         | SF                     | 311   | 33        | 5      | x    |              | 2024-09-01      |       |
| 6         | Boston (2023-present)  | Crime | 13        | 6      | x    |              |                 |       |
| 6         | Boston 2025            | 311   | 200       | 6      | x    |              |                 |       |
| 7         | Los Angeles            | Crime | 187       | 7      | x    | weekly       | 2024-12-01      |       |
| 7         | Los Angeles 2025       | 311   | 206       | 7      | x    | daily        | 2025-01-01      |       |
| 8         | Houston 2025           | Crime | 212       | 8      | x    | monthly      | 2025-01-01      |       |
| 8         | Houston MTD            | 311   | 191       | 8      | x    | daily        |                 |       |
| 9         | Philadelphia 2025      | crime | 193       | 9      | x    | daily        |                 |       |
| 9         | Philadelphia 2025      | 311   | 194       | 9      | x    | daily        |                 |       |
| 10        | San Diego              | crime | 99        | 10     | x    | daily        |                 |       |
| 10        | San Diego 2025         | 311   | 192       | 10     | x    | daily        |                 |       |
| 11        | Charlotte              | crime | 105       | 11     | x    | daily        |                 |       |
| 11        | Charlotte              | 311   | 106       | 11     | x    | daily        |                 |       |
| 12        | Denver                 | crime | 107       | 12     | x    | daily        |                 |       |
| 12        | Denver                 | 311   | 108       | 12     | x    | daily        |                 |       |
| 13        | Detroit                | crime | 109       | 13     | x    | daily        |                 |       |
| 13        | Detroit                | 311   | 110       | 13     | x    | daily        |                 |       |
| 14        | Memphis                | crime | 113       | 14     | x    | daily        | 2024-12-01      |       |
| 14        | Memphis                | 311   | 215       | 14     | x    | daily        | 2023-01-01      |       |
| 15        | Montgomery County      | crime | 117       | 15     | x    | daily        | 2024-12-01      |       |
| 16        | Nashville              | crime | 118       | 16     | x    | daily        |                 |       |
| 16        | Nashville (YTD)        | 311   | 120       | 16     | x    | daily        |                 |       |
| 17        | Kansas City 2025       | crime | 208       | 17     | x    | daily        | 2025-01-01      |       |
| 17        | Kansas City            | 311   | 124       | 17     | x    | daily        | 2024-12-01      |       |
| 18        | Oakland (last 90 days) | crime | 125       | 18     | x    | daily        |                 |       |
| 18        | Oakland                | 311   | 128       | 18     | x    | daily        | 2024-12-01      |       |
| 19        | Minneapolis 2025       | crime | 201       | 19     | x    | daily (year) |                 |       |
| 19        | Minneapolis 2025       | 311   | 202       | 19     | x    | daily (year) |                 |       |
| 20        | Cleveland              | crime | 139       | 20     | x    | daily (full) |                 |       |
| 20        | Cleveland              | 311   | 140       | 20     | x    | daily (full) |                 |       |
| 21        | Cincinnatti            | crime | 213       | 21     | x    | daily        | 2024-06-01      |       |
| 21        | Cincinnatti            | 311   | 144       | 21     | x    | daily        |                 |       |
| 22        | St. Louis 2025         | crime | 207       | 22     | x    | daily (year) |                 |       |
| 22        | St. Louis              | 311   | 210       | 22     | x    | daily (year) |                 |       |
| 23        | Baltimore              | crime | 209       | 23     | x    | daily (full) |                 |       |
| 23        | Baltimore 2025         | 311   | 203       | 23     | x    | daily (year) |                 |       |
| 24        | Washington DC 2025     | crime | 204       | 24     | x    | daily        |                 |       |
| 24        | Washington DC 2025     | 311   | 205       | 24     | x    | daily        |                 |       |
| 25        | Prince George's        | crime | 188       | 25     | x    | daily        | 2024-12-01      |       |
| 25        | Prince George's        | 311   | 172       | 25     | x    | daily        |                 |       |
| 26        | Baton Rouge            | crime | 175       | 26     | x    | daily        |                 |       |
| 26        | Baton Rouge            | 311   | 176       | 26     | x    | daily        |                 |       |
| 27        | Buffalo, NY            | crime | 180       | 27     | x    | daily        | 2024-12-01      |       |
| 27        | Buffalo, NY            | 311   | 181       | 27     | x    | daily        | 2024-12-01      |       |
| 28        | Chattanooga            | crime | 184       | 28     | x    | daily        | 2024-12-01      |       |
| 28        | Chattanooga            | 311   | 185       | 28     | x    | daily        | 2024-12-01      |       |
| 29        | Seattle                | crime | 190       | 29     | x    | daily        | 2024-12-01      |       |
| 30        | Portland, OR           | crime | 214       | 30     | x    | daily        | 2025-01-01      |       |


#### Submit New Worker Task

 `./ops/update_sources.sh`: has current list of sources; submits new jobs.

* For each source.id above, submit to create new api task:
  * `source.id`: `curl -H 'content-type:application/json' -H 'X-API-KEY:<API-KEY>' localhost:8080/sources`
  * `curl  -X POST -H 'content-type:application/json' -H 'X-API-KEY: <API-KEY>' localhost:8080/datajobs/sources/:source.id`


## Create New Source / Locale / TextCategories Operations

See `./classifer/README.md` for locale -> source -> textcategory sequence.

### Update Errant Source

* Make any `source_config.json`changes - includes any mapping updates
* Submit to update route:
  * `curl -X PATCH -H 'content-type:application/json' -H 'X-API-KEY: <key>' -d @source_config.json api.311crimemap.com/sources/<id>`
  * submit full source (updates all) object not single fields

##### Mapping

Mapping has optional fields that can be ignored for most types, but required for
specific `dataParserType`:

* `CSVCUSTOM`: `dataParserDelimeter`, `dataParserNumSkip`


### Detect Remove Duplicates

##### Detect

* `select count(*), report_num, source_id from data_crime group by report_num, source_id having count(report_num) >= 2`
* `select count(*) from (select count(*), report_num, source_id from data_crime group by report_num, source_id having count(report_num) >= 2);`
* `select distinct(source_id) from (select count(*), report_num, source_id from data_crime group by report_num, source_id having count(report_num) >= 2);`

#### DELETE

Best to filter via `source_id` grabbed from distinct query above to speed things
up. Replace 'X' with `source_id`.

```
DELETE FROM data_crime
WHERE source_id = X AND id NOT IN (
    SELECT MIN(id)
    FROM data_crime WHERE source_id = X
    GROUP BY report_num, source_id
);
```

