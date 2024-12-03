# API Sources

Scripts are to extract a list of crime and 311 data sources. e.g. police
department and 311 data urls.

End results:

* `data_311.csv`
* `data-crime.csv`

Go through each and verify is valid data source, eventually create Source
objects to submit to workers for scrape. (see `../source-config`)

This is a pretty outdated, incomplete list, so take what's available from the
above files but don't revisit this approach.

---

## Approach

[Catalog](https://api.us.socrata.com ) - is incomplete, almost like the index
was never updated for many cities. Take what can be taken, but this is not a
complete source.

Additional Data Source Index

* https://www.opendatanetwork.com/search?q=crime+data
* https://api.us.socrata.com/api/catalog/v1?q=crime%20data&offset=0&limit=10


#### Raw data

1. query by 'crime', 'police'

https://api.us.socrata.com/api/catalog/v1?q=crime%20police&offset=0&limit=10000&&min_should_match=1&only=dataset&order=updatedAt


2. query by '311'

https://api.us.socrata.com/api/catalog/v1?q=311&limit=10000&only=dataset&order=updatedAt

write to `311-raw.json`, `police-crime-raw.json`


3. filter to catalog:

* `jq '.results' 311-raw.json > 311-catalog.json`
* `jq '.results' police-crime-raw.json > police-crime-catalog.json`

4. `1-candidate-catalog.py`: get open AI classification of dataset

5. `2-catalog-to-csv.py`: convert `*-catalog.json` to csv

6. `out-311.json`, `out-crime.json` filter label == true and match back in excel.

* `cat out-311.json  | jq '.[] | select(.label == true) ' | jq -r '.resource_id'`
* `cat out-crime.json  | jq '.[] | select(.label == true) ' | jq -r '.resource_id'`

7. Insert back into `data-311.csv` / `data-crime.csv` (generated in step 5).

8. Copy and manual verify

## Labeling

Tried labeling, but for the most part easiest to just parse through line by line
and filter out relevant datasets. Lots of irrelevant or aged out datasets.

Many cities are not indexed in the catalog, so the catalog and related datasets
are mostly a secondary list. suggest searching by largest cities / counties if
has data. Kind of a waste of time.


* `1-candidate-catalog.py`: sends metadata to OpenAI for classification (see
  `./prompt`) whether relevant data set.
* `2-catalog-to-csv.py`: converts raw json output to truncated csv for (easier)
  excel browsing.



## How to Paginate Results

Deep scroll pagination - any results from Catalog API are limited to 10000.

(NB: not using this, relying on API query result set vs self filtering catalog.)

https://dev.socrata.com/docs/other/discovery#?route=get-/catalog/v1-limit--number--scroll_id--id-

* `wget 'https://api.us.socrata.com/api/catalog/v1?limit=10000&scroll_id=ti3t-pm8k' -O catalog-N.json`

* `jq -r '.results[].resource.id' catalog-N.json > ids-N.json`

catalog-filtered.json is truncated fields (same records) of catalog.json - to reduce token counts.

---
