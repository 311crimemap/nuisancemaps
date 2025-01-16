# Source Config

* Restart Jobs within past day: `curl -H "X-API-KEY: $ADMIN_API_KEY" api.311crimemap.com/datajobs/restart`

## Quick Submit Prod

From completed / verified dev to prod:

0. log worker

* `kubectl logs -f <worker pod>`

1. Did you build latest image and deploy?

* `./mvnw spring-boot:build-image -Dmaven.test.skip=true -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher`
* `docker push $IMAGE_REPO/311crimemap/api:<TAG>`

* `kubectl rollout restart deployment/spring-worker`
* `kubectl rollout restart deployment/spring-api`

---

1. submit text categories

* `curl -X POST -H 'content-type: application/json' -H "X-API-KEY: $ADMIN_API_KEY" -d @text_categories.txt.out.csv.final.json https://api.311crimemap.com/textcategories`

2. submit locale

* `curl -X POST -H 'content-type: application/json' -H "X-API-KEY: $ADMIN_API_KEY" -d @locale.json https://api.311crimemap.com/locales`

3. submit source (source_config.json)

* `curl -X POST -H 'content-type: application/json' -H "X-API-KEY: $ADMIN_API_KEY" -d @source_config.json https://api.311crimemap.com/locales/{id}/sources`

Restart Job:

`curl -X PATCH -H "content-type: application/json" -H "X-API-KEY: $ADMIN_API_KEY" -d '{"status":"QUEUED"}' api.311crimemap.com/datajobs/<dataJobId>`

---

General rule, use csv if data is already broken up in yearly increments. JSON if
repeat, or partial subset query is needed.

Try on dev first if there's a custom method - breaks way too often.

## Condensed

#### Field Align

* copy and fill `meta.json` to <DIR>
* `./00-generate.sh <DIR>` (calls 0-3 python scripts)
* `python fields.py <DIR>`
* edit `source_config.json` for fields
  * add any custom generated methods
* create `locale.json` if needed

#### Text Category

* `./01-text-cat.sh`
* view output `text_categories.txt.out.csv`, edit
  `text_categories.txt.out.csv.final.json` for any changes.

## General Process

1. [spreadsheet] base url → determine downloadable json and/or csv url

3. Create directory `data/098-city-crime`, `/data/099-city-311` pattern

4. Populate `meta_template.json` -> edit and copy to directory `meta.json`
   * zipfile: add `?zipfile=<file-to-extract.csv>`
   * arcgis: - make sure hub.arcgis url param has `redirect=true`

5. Download: `0-download.py <dir>` -> downloads sample data to `data.json` or `data.csv`

6. Submit openAI: `1-generate-source-config.py <dir>`

7. Submit openAI: `2-generate-source-methods.py <dir>`

8. Review generated source_config in `data.csv.out.json` / `data.json.out.csv` amend for proper fields / methods
   * `csvlook data.csv`
   * `csvcut -c "Field1,Field2" data.csv`
   * change url, id; add any methods to service/parserstrategy
     * `ParserStrategy.java`  - enum
     * `ParserStrategyConfig.java` - mapping
     * `ParserStrategyConfig<City>.java` - method

9. Save `source_config.json` (from `data.*.out.json`) to track final submit vs
   generated/modified

10. Create `locale.json` if needed (from `/data/locale_template.json`)

11. Text Category Workflow:

* `3-text-category-fetch.py <dir>` -> possibly downloads `data-full.csv`, but
  generates `text_categories.txt`

* `4-text-category-classifier.py` -> saves labels to
  `text_categories.txt.out.json` and converts `text_categories.txt.out.csv` -
  for easier edit and verification

* VERIFY `text_categories.txt.out.csv` IN EXCEL

* `5-text-category-to-json-for-submit.py` ->
  `text_categories.txt.out.csv.final.json` for submit

12. Submit, test, verify worker on dev

* submit text categories: `curl -X POST -H 'content-type: application/json' -H 'X-API-KEY: 1234' -d @text_categories.txt.out.csv.final.json localhost:8080/textcategories`

* submit locales: `curl -X POST -H 'content-type: application/json' -H 'X-API-KEY: 1234' -d @locale.json localhost:8080/locales`

* submit source config: what was `data.csv.out.json` (NB: triggers worker)
  * `curl -X POST -H 'content-type: application/json' -H 'X-API-KEY: 1234' -d @source_config.json 'localhost:8080/locales/{id}/sources'`

13. Fixes and submit to prod

14. Mark if source is a daily runner on ./PRODUCTION.md

15. `copy_data_s3.sh`: upload /data directory to s3 bucket

16. If city is complete, remove from dev

---

## Source urls pattern from id key

* CSV: https://data.buffalony.gov/api/views/d6g9-xbgu/rows.csv?accessType=DOWNLOAD
* JSON: https://data.buffalony.gov/resource/d6g9-xbgu.json?$limit=2


## Template -> Source Config

### Download

`0-download.py <dir>` takes `meta.json` url and downloads sample
`data.json` or `data.csv`.

### Prompt

`./prompt/.1-gen-source-config.txt`:

#### Obtaining User Data

Snippet of JSON or CSV data from url source e.g $?limit=3 (Example commands)

* JSON: `curl -s 'https://data.buffalony.gov/resource/d6g9-xbgu.json?$limit=3' > buffalo.json`
* CSV: `curl -s 'https://data.buffalony.gov/api/views/d6g9-xbgu/rows.csv?accessType=DOWNLOAD' | awk 'NR > 3 { exit } { print }' > buffalo.csv`

NB: use awk here since `curl | head -n 3` doesn't (OS Specific) always send
SIGPIPE to indicate end of process, so curl could continue to download in
background. Does seem to work in ubuntu, but using awk for robustness.

Submit ~3 examples as user role.

---

## Sample Data -> Source Config

* `./1-generate-source_config.py <data_directory> <sample_data.json>`
* e.g.: `./1-generate-source-config.py data/039-buffalo-crime/ data/039-buffalo-crime/buffalo.json`

Requires correct `meta.json`.

## Source Config -> Method Gen

* `./2-generate-source-methods.py <data_directory> <sample_data.json>`
* e.g.: `./2-generate-source-methods.py data/039-buffalo-crime/ data/039-buffalo-crime/buffalo.json`

### Prompt

`./prompt/.2=generate-source-methods.txt`:

USER INPUT:

1. source config json from previous step (`1-generate-source-config.py`)
2. sample data Same as above snippet of json or csv data

---

## Verify; add any method gen, use in Source Config.

* Determine if `*.json.methods.txt` has separate methods
* add methods to api `service/parserstrategy/*.java` city methods page.
* add source_config to source-config.json
* submit and verify

---

### Data Notes

`./data` contains downloads, scratch.

From `001-*` to `039-*` were original "scratch" datasets cobbled together
without much process.

The data is valid, but they're the results of old extraction scripts
(textcategory) approach of passing "crime" and "311" parameter.

From `040-*`, using the source-config process outlined above.


---


## Adding Error TextCategories

Reminder many error messages will end up being duplicates, so it's less intimidating than it looks.

NB: if getting parse errors, proper json has no dangling ','.

* Collect errors:
  * ~~`curl -H 'X-API-KEY: <KEY>' localhost:8080/dataerrors | jq '.[].errorMsg`~~
  * `curl -H 'X-API-KEY: <KEY>' localhost:8080/pendingtextcategories?type=crime | jq -r '.data[].text'`

* Take each type, category and start label process for submission
* Either manually label category, or submit to openAI

Manual Example
* `labeled_crime.json` -> copy to `missing_crime.json`, for example: add
  category and label (and dataType) and submit.

OpenAI Crime Example
* add to `data_crime.txt`
* `classifier.py`
* review `out_crime.json` (jq to excel) -> `labeled_crime.csv`
* `convert_csv_to_json.py` -> `labeled_crime.json`

Submit

`curl -X POST -d @text_categories.txt.out.csv.final.json -H 'content-type: application/json' -H 'X-API-KEY: <KEY>' localhost:8080/textcategories`



## Helpful Queries

Sometimes duplicate TextCategory records might occur (shouldn't with newly
fixed constraints)...but just in case:

Quick query to count duplicates and return the id and category.

Decide which label is incorrect and delete the record from the table.

```
SELECT
    tc.text,
    COUNT(*) as occurrence_count,
    STRING_AGG(tc.id::text, ', ') AS ids,
    STRING_AGG(c.id::text, ', ') AS category_ids,
    STRING_AGG(c.text, ', ') AS category_names
FROM
    text_category tc
JOIN
    category c ON tc.category_id = c.id
GROUP BY
    tc.text
HAVING
    COUNT(*) > 1
ORDER BY
    occurrence_count DESC,
    tc.text;

```
