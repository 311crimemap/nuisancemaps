# Worker Data

---

## Data

Based on OpenData collections.

To find the url, find the data page, then click API

| Location | Crime Type | Crime                                                  | 311                                                    |
| -------- | ---------- | ------------------------------------------------------ | ------------------------------------------------------ |
| ATX      | complaint  | https://data.austintexas.gov/resource/fdj4-gpfu.json   | https://data.austintexas.gov/resource/xwdj-i9he.json   |
| NYC      | complaint  | https://data.cityofnewyork.us/resource/5uac-w243.json  | https://data.cityofnewyork.us/resource/erm2-nwe9.json  |
| NYC      | arrests    | https://data.cityofnewyork.us/resource/uip8-fykc.json  | -                                                      |
| CHI      | complaint  | https://data.cityofchicago.org/resource/ijzp-q8t2.json | https://data.cityofchicago.org/resource/v6vf-nfxy.json |
| SF       | complaint  | https://data.sfgov.org/resource/wg3w-h783.json         | https://data.sfgov.org/resource/vw6y-z8j6.json         |


NYC: Also historic - need to decide what's being supported
https://data.cityofnewyork.us/Public-Safety/NYPD-Complaint-Data-Historic/qgea-i56i
https://data.cityofnewyork.us/Public-Safety/NYPD-Arrests-Data-Historic-/8h9b-rp9u


ATX crime need sort
https://data.austintexas.gov/resource/fdj4-gpfu.json?$query=SELECT%20*%20ORDER%20BY%20%60rep_date_time%60%20DESC%20NULL%20LAST

311
https://data.austintexas.gov/resource/xwdj-i9he.json

https://dev.socrata.com/docs/datatypes/#,

## Tables

Table Names:

* data_crime
* data_311
* source
* city_state
* zipcode

have to assume everything might be missing at some point

### data_crime

| field                 | crime data field                                                                    |
| --------------------- | ----------------------------------------------------------------------------------- |
| id                    | -                                                                                   |
| external_report_id    | complaint_num, case_num, report_num, etc.                                           |
| source_id             | (fkey source table)                                                                 |
| incident type / name  | of_desc, primary_type, crime_type,                                                  |
| incident description  | pd_desc, description                                                                |
| location_type              | prem_type_desc, location_description, location_type, (general location description) |
| date reported created | rpt_dt, date, rep_date_time,                                                        |
| latitude              | latitude, location.latittude                                                        |
| longitude             | longitude, location.longitude                                                       |
| Point                 | spatial                                                                             |
| created_at            |                                                                                     |
| updated_at            |                                                                                     |

### data_311

| field                | 311 data field                                                              |
| -------------------- | --------------------------------------------------------------------------- |
| id                   | -                                                                           |
| external_report_id   | sr_num, unique_key, service_request_id, sr_number                           |
| source_id            | (fkey source table)                                                         |
| incident type / name | sr_type_desc, complaint_type, service_name, sr_type                         |
| incident description | descriptor, service_detail, detail (may not exist, be external to response) |

These require double crawls to check for update - skip? except for date created
query by last_update and update fields?
311 app filters by new / open / closed but no one gives a shit?

| status                | status, sr_status_desc, status_description                                  | necessary?                                |
| date reported created | created_date, sr_created_date, requested_date_time                          | necessary?                                |
| last update?          | updated_datetime, sr_updated_date -- now we have to check on this?          | necessary?                                |
| date reported closed  | closed_date,                                                                | necessary?                                |

what use is this - address is good to display
somehwat redundant, except on global level - just do it

| street_address_raw    | street_address                                                              | raw                                       |
| street_address        | generated street address compound                                           | should this be method or created on input? |
| city_state_id         | fkey city_state                                                             | necessary?                                |
| zipcode_id            | fkey zipcode                                                                | necessary?                                |

| latitude              | latitude, location.latittude                                                |
| longitude             | longitude, location.longitude                                               |
| Point                 | spatial                                                                     |
| created_at            |                                                                             |
| updated_at            |                                                                             |


### source (311 or crime data source meta)

| field                         | 311 data field |
| ----------------------------- | -------------- |
| id                            | -              |
| external_report_id            |                |
| report (311, crime) type      |                |
| description (nyc, atx, detc.) |                |
| url                           |                |
| created_at                    |                |
| updated_at                    |                |

field config - premature; just do hardcode in code for now, see where refactor can happen


### city_state (from 311)

| field | data field        |
| ----- | ----------------- |
| id    | -                 |
| city  | city              |
| state | state  (hardcode) |


#### zipcode (from 311)

| field   | data field                              |
| ------- | --------------------------------------- |
| id      | -                                       |
| zipcode | incident_zip, zip_code, (regex extract) |


Some places don't use addresses, just general spots on the road

- query limited, ingest limited
- skip saving primary raw data, don't want to deal with redoing all the fields
- Save raw json to s3? postgres json


---

## Entity Notes

Process:

1. design entity
2. design data -> input code
3. local file to test Entity then curl
4. refactor for DI