# Data

## Collections

Based on OpenData collections. What a mess. :\

https://www.opendatanetwork.com/
https://catalog.data.gov/dataset/?res_format=JSON

## Data Notes

### Update Frequency Sample

Shouldn't expect any real consistency with updates. Each "update" is typically a
new view. Records can be removed or re-ordered. So cannot expect to just pick up
at position X and get new records.

Update frequency varies. From daily, weekly, or every quarter.

#### Size

| Entity     | Rows   | Dates        | Years | DB Size | Estimate DB |
|------------|--------|--------------|-------|---------|-------------|
| APD        | 1.80 M | 2003 ->      | 20+   | 0.9 GB  | .9 GB       |
| Austin 311 | 2.45 M | 2014 ->      | 9+    | 1.1 GB  | 1.2 GB      |
| NYC 311    | 35.6 M | 2006 -> 2019 | 13    |         | 17.0 GB     |
| NYC Crime  | 8.4  M | 2010 ->      | 13+   |         | 4.2         |
| Chi Crime  | 8.0  M | 2001 -?      | 23+   |         | 4.0         |
| Chi 311    | 9.5  M | 2018 ->      | 6+    |         | 5.0         |
| SF crime   | 1 M    | 2018         | 6+    |         | .5          |
| SF 311     | 7 M    | 2008         | 15+   |         | 3.5         |
|            |        |              |       |         |             |

Total: 36.5 GB
~ 5GB a city for both 311 and crime?
~ 10M rows per city


### URLS

To find the url, find the data page, then click API

| Location | Crime Type | Crime                                                  | 311                                                    |
| -------- | ---------- | ------------------------------------------------------ | ------------------------------------------------------ |
| ATX      | complaint  | https://data.austintexas.gov/resource/fdj4-gpfu.json   | https://data.austintexas.gov/resource/xwdj-i9he.json   |
| NYC | complaint historic | https://data.cityofnewyork.us/resource/qgea-i56i.json | 
| NYC      | complaint YTD  | https://data.cityofnewyork.us/resource/5uac-w243.json  | https://data.cityofnewyork.us/resource/erm2-nwe9.json  |
| NYC      | arrests    | https://data.cityofnewyork.us/resource/uip8-fykc.json  | -                                                      |
| CHI      | complaint  | https://data.cityofchicago.org/resource/ijzp-q8t2.json | https://data.cityofchicago.org/resource/v6vf-nfxy.json |
| SF       | complaint  | https://data.sfgov.org/resource/wg3w-h783.json         | https://data.sfgov.org/resource/vw6y-z8j6.json         |


NYC: Also historic - need to decide what's being supported
https://data.cityofnewyork.us/Public-Safety/NYPD-Complaint-Data-Historic/qgea-i56i
https://data.cityofnewyork.us/Public-Safety/NYPD-Arrests-Data-Historic-/8h9b-rp9u


### Helpful Queries

Count: `$query=SELECT count(*)`

Sort `$query=SELECT * ORDER BY rep_date_time DESC NULL LAST`

Distinct limit: `$query=SELECT distinct offincident limit 10000`

Similar to 'distinct': `$query=SELECT sr_type_desc group by sr_type_desc`

Exclude records with no coordinates - they won't be processed, there are too
many so they pollute logs; default is to skip anyway

* `$query=SELECT complaint_type WHERE latitude > 0 GROUP BY complaint_type LIMIT 5000`
* `$query=SELECT service_name WHERE lat > 0 GROUP BY service_name LIMIT 5000`


311
https://data.austintexas.gov/resource/xwdj-i9he.json

https://dev.socrata.com/docs/datatypes/

##### Address ?

| Entity      | Address                                                              |
|-------------|----------------------------------------------------------------------|
| APD         | y                                                                    |
| Austin 311  | y                                                                    |
| Chicago PD  | obfuscated, precise enough via lat/lng reverse geocoding             |
| Chicago 311 | y                                                                    |
| SF Crime    | no (Just intersection), precise enough via lat/lng reverse geocoding |
| SF 311      | y                                                                    |
| NYC crime   | iffy - lat/lng not so precise, depends on geocoder                   |
| NYC 311     | y                                                                    |


