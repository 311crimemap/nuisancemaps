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


| Source           | type  | source.id | locale | init submit | freq                         |
|------------------|-------|-----------|--------|-------------|------------------------------|
| Austin           | Crime | 2         | 1      | x           |                              |
| Austin           | 311   | 35        | 1      | x           |                              |
| Dallas           | Crime | 36        | 2      | x           |                              |
| Dallas           | 311   | 37        | 2      | x           |                              |
| Chicago          | Crime | 39        | 3      | x           |                              |
| Chicago          | 311   | 34        | 3      | x           |                              |
| NYC              | Crime | 11        | 4      | x           | quarterly last update Oct 21 |
| NYC              | 311   | 32        | 4      | x           |                              |
| SF               | Crime | 38        | 5      | x           |                              |
| SF               | 311   | 33        | 5      | x           |                              |
| Boston           | Crime | 13        | 6      | x           |                              |
| Boston           | 311   | 22        | 6      | x           |                              |
| Los Angeles      | Crime | 40        | 7      | x           | weekly (csv)                 |
| Los Angeles 2024 | 311   | 41        | 7      | x           | daily                        |
| Houston 2024     | Crime | 46        | 8      | x           | monthly                      |
|                  |       |           |        |             |                              |

#### Submit New Worker Task

For each source.id above, submit to create new api task:

* `source.id`: `curl -H 'content-type:application/json' -H 'X-API-KEY:<API-KEY>' localhost:8080/sources`
* `curl  -X POST -H 'content-type:application/json' -H 'X-API-KEY: <API-KEY>' localhost:8080/datajobs/sources/:source.id`

---


## Create New Source / Locale / TextCategories

see `./classifer/README.md`. Broad strokes below.

### New Locale

* copy `classifier/source-config/data/locale_template.json` -> `data/<dir>/locale.json`
* Fill in fields

### New Source

Follow `./classifier/source-config/` sequence:

* `0-download.py`
* `1-generate-source-config.py`
* `2-generate-source-methods.py`

### For Text Categories (continued):

* `3-text-category-fetch.py`
* `4-text-category-classifier.py`
  * * EXCEL Verify and relable columns: `| dataType | text | label |`
* `5-text-category-to-json-for-submit.py`

### Update Errant Source

* Make any `source_config.json`changes - includes any mapping updates
* Submit to update route:
  * `curl -X PATCH -H 'content-type:application/json' -H 'X-API-KEY: <key>' -d @source_config.json api.311crimemap.com/sources/<id>`
  * submit full source (updates all) object not single fields


##### Mapping

Mapping has optional fields that can be ignored for most types, but required for
specific `dataParserType`:

* `CSVCUSTOM`: `dataParserDelimeter`, `dataParserNumSkip`


---

## Pending Text Category -> Text Category

see `./classifer/source-config`: similar steps but from `/pendingtextcategories`
endpoint, becomes source agnostic.

1. Extract data to particular `pending_crime_<date>` / `pending_311_<date>`
   directory to `text_categories.txt`:

* `curl -H 'X-API-KEY: <KEY>' api.311crimemap.com/pendingtextcategories?type=crime | jq -r '.data[].text' > text_categories.txt`
* `curl -H 'X-API-KEY: <KEY>' api.311crimemap.com/pendingtextcategories?type=3131 | jq -r '.data[].text' > text_categories.txt`

2. Follow text category sequence:

* `4-text-category-classifier.py`
  * * EXCEL Verify and relable columns: `| dataType | text | label |`
* `5-text-category-to-json-for-submit.py`
