# Production Operations

## Build

### Web

1. verify `npm run build` passess

2. merge `master` in `deploy/staging`, `deploy/production` branch, Cloudflare
   Pages takes over build

### API

1. `aws ecr get-login-password --region us-east-2 --profile 311crimemap | \
docker login --username AWS --password-stdin 058264272856.dkr.ecr.us-east-2.amazonaws.com`

2. `./mvnw spring-boot:build-image -Dmaven.test.skip=true -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher` (not in container)
   * might need to remove /target via sudo

3. docker push <image>


## Operations

Instructions on daily operations to run.

* To build list of source ids for daily update:
  * `curl -H 'X-API-KEY: <KEY>' -H 'content-type:application/json'  api.311crimemap.com/sources | jq`
  * NB: exclude large `dataProcessType: FILE` archives and use JSON subset by report
    date for city.

* To create a new source, look at
  `api/src/main/resources/data/source_config.json` to find an example per type -
  typically a `BASE` type starter and then an `OPENDATADATE` type for daily update.

* Add to source_config.json, and submit individual source id:

```
# add new source

curl -X POST -H 'content-type: application/json' -H 'X-API-KEY: <KEY>' \
-d @source.json localhost:8080/locales/{id}/sources
```

### Daily Runs: production-blue ids


| Source  | type  | source.id | locale | init submit | freq                         |
|---------|-------|-----------|--------|-------------|------------------------------|
| Austin  | Crime | 2         | 1      | x           |                              |
| Austin  | 311   | 35        | 1      | x           |                              |
| Dallas  | Crime | 36        | 2      | x           |                              |
| Dallas  | 311   | 37        | 2      | x           |                              |
| Chicago | Crime | 39        | 3      | x           |                              |
| Chicago | 311   | 34        | 3      | x           |                              |
| NYC     | Crime | 11        | 4      | x           | quarterly last update Oct 21 |
| NYC     | 311   | 32        | 4      | x           |                              |
| SF      | Crime | 38        | 5      | x           |                              |
| SF      | 311   | 33        | 5      | x           |                              |
| Boston  | Crime | 13        | 6      | x           |                              |
| Boston  | 311   | 22        | 6      | x           |                              |


#### Submit New Worker Task

For each source.id above, submit to create new api task:

* `source.id`: `curl -H 'content-type:application/json' -H 'X-API-KEY:<API-KEY>' localhost:8080/sources`
* `curl  -X POST -H 'content-type:application/json' -H 'X-API-KEY: <API-KEY>' localhost:8080/datajobs/sources/:source.id`

---

## Create New Source

### PRE Submit New TextCategory per Source

see `./classifer`:

Extract data:
* `column` is category name, `type` is 311 or crime
* `curl <datasource>?$select=<column>&$group=<column>&$limit=100000 > data_<type>.json`

Follow classifier sequence:
* `0-data.py`
* `1-classifier.py`
* `2-convert_out_to_csv.py`
* EXCEL Verify columns: `| dataType | text | label |` - compare with `classifer/config/categories_<type>.txt`
  * add SKIP as category + 1
* `3-convert_csv_to_json.py`
* Submit to api:
  * `curl -X POST -H 'content-type: application/json' -H 'X-API-KEY: ...' -d @labeled_311.json localhost:8080/textcategories`

### Create new Source

1. Find locale

` curl localhost:8080/locales`

The id becomes `locale_id` below

2. Submit new source:

* `@new_source.json`: source file; make sure its in `source_config.json` for archive.
* `curl -X POST -H 'content-type:application/json' -H 'X-API-KEY: <KEY>' -d @new_source.json localhost:8080/locales/<locale_id>/sources`

3. Update source (e.g. update 'startReportedAt' Mapping date)

* Extract and change source: `cat source_config.json | jq '.[38]' > test.json`
  * want to send the full updated object with changes
* Edit `test.json` with changes; any mapping values will overwrite as well
* Submit to update route:
  * `curl -X PATCH -H 'X-API-KEY: <key>' -H 'content-type:application/json' -d @38.json api.311crimemap.com/sources/38`



---

## Pending Text Category -> Text Category

see `./classifer` - same steps  as new source once data is extracted:

Extract data:

* `curl -H 'X-API-KEY: <KEY>' api.311crimemap.com/pendingtextcategories?type=crime | jq -r '.data[].text'`
* `curl -H 'X-API-KEY: <KEY>' api.311crimemap.com/pendingtextcategories?type=3131 | jq -r '.data[].text'`

Follow classifier sequence:
* `0-data.py`
* `1-classifier.py`
* `2-convert_out_to_csv.py`
* EXCEL Verify columns: `| dataType | text | label |` - compare with `classifer/config/categories_<type>.txt`
  * add SKIP as category + 1
* `3-convert_csv_to_json.py`
* Submit to api
  * `curl -X POST -H 'content-type: application/json' -H 'X-API-KEY: ...' -d @labeled_311.json localhost:8080/textcategories`
