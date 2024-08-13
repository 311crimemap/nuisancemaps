# Data

Starting from 2015.

Crime: https://data.boston.gov/dataset/crime-incident-reports-august-2015-to-date-source-new-system
311: https://data.boston.gov/dataset/311-service-requests


## TEXTCATEGORY URLS:

Crime Template:  https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "OFFENSE_DESCRIPTION" from "313e56df-6d77-49d2-9c49-ee411f10cf58"

311 Template: https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "e6013a93-1321-4f2a-bf91-8d8a02f1e62f"


| Title        | TextCategory Distinct URL                                                                                                                       |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| Crime 2023-> | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "OFFENSE_DESCRIPTION" from "b973d8cb-eeb2-4e7e-99da-c92938efc9c0" |
| Crime 2022   | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "OFFENSE_DESCRIPTION" from "313e56df-6d77-49d2-9c49-ee411f10cf58" |
| Crime 2021   | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "OFFENSE_DESCRIPTION" from "f4495ee9-c42c-4019-82c1-d067f07e45d2" |
| Crime 2020   | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "OFFENSE_DESCRIPTION" from "be047094-85fe-4104-a480-4fa3d03f9623" |
| Crime 2019   | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "OFFENSE_DESCRIPTION" from "34e0ae6b-8c94-4998-ae9e-1b51551fe9ba" |
| Crime 2018   | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "OFFENSE_DESCRIPTION" from "e86f8e38-a23c-4c1a-8455-c8f94210a8f1" |
| Crime 2017   | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "OFFENSE_DESCRIPTION" from "64ad0053-842c-459b-9833-ff53d568f2e3" |
| Crime 2016   | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "OFFENSE_DESCRIPTION" from "b6c4e2c3-7b1e-4f4a-b019-bef8c6a0e882" |
| Crime 2015   | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "OFFENSE_DESCRIPTION" from "792031bf-b9bb-467c-b118-fe795befdf00" |
| 311 2024     | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "dff4d804-5031-443a-8409-8344efd0e5c8"                |
| 311 2023     | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "e6013a93-1321-4f2a-bf91-8d8a02f1e62f"                |
| 311 2022     | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "81a7b022-f8fc-4da5-80e4-b160058ca207"                |
| 311 2021     | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "f53ebccd-bc61-49f9-83db-625f209c95f5"                |
| 311 2020     | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "6ff6a6fd-3141-4440-a880-6f60a37fe789"                |
| 311 2019     | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "ea2e4696-4a2d-429c-9807-d02eb92e0222"                |
| 311 2018     | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "2be28d90-3a90-4af1-a3f6-f28c1e25880a"                |
| 311 2017     | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "30022137-709d-465e-baae-ca155b51927d"                |
| 311 2016     | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "b7ea6b1b-3ca4-4c5b-9713-6dc1db52379a"                |
| 311 2015     | https://data.boston.gov/api/3/action/datastore_search_sql?sql=SELECT DISTINCT "type" from "c9509ab4-6f6d-4b97-979a-0cf2a10c922b"                |


cat 311.json |jq -r '.result.records[].type'
cat crime.json | jq -r '.result.records[].OFFENSE_DESCRIPTION'

## Data URLS


| Title        | URL                                                                                  |
|--------------|--------------------------------------------------------------------------------------|
| Crime 2023-> | https://data.boston.gov/datastore/dump/b973d8cb-eeb2-4e7e-99da-c92938efc9c0?bom=True |
| Crime 2022   | https://data.boston.gov/datastore/dump/313e56df-6d77-49d2-9c49-ee411f10cf58?bom=True |
| Crime 2021   | https://data.boston.gov/datastore/dump/f4495ee9-c42c-4019-82c1-d067f07e45d2?bom=True |
| Crime 2020   | https://data.boston.gov/datastore/dump/be047094-85fe-4104-a480-4fa3d03f9623?bom=True |
| Crime 2019   | https://data.boston.gov/datastore/dump/34e0ae6b-8c94-4998-ae9e-1b51551fe9ba?bom=True |
| Crime 2018   | https://data.boston.gov/datastore/dump/e86f8e38-a23c-4c1a-8455-c8f94210a8f1?bom=True |
| Crime 2017   | https://data.boston.gov/datastore/dump/64ad0053-842c-459b-9833-ff53d568f2e3?bom=True |
| Crime 2016   | https://data.boston.gov/datastore/dump/b6c4e2c3-7b1e-4f4a-b019-bef8c6a0e882?bom=True |
| Crime 2015   | https://data.boston.gov/datastore/dump/792031bf-b9bb-467c-b118-fe795befdf00?bom=True |
| 311 2024     | https://data.boston.gov/datastore/dump/dff4d804-5031-443a-8409-8344efd0e5c8?bom=True |
| 311 2023     | https://data.boston.gov/datastore/dump/e6013a93-1321-4f2a-bf91-8d8a02f1e62f?bom=True |
| 311 2022     | https://data.boston.gov/datastore/dump/81a7b022-f8fc-4da5-80e4-b160058ca207?bom=True |
| 311 2021     | https://data.boston.gov/datastore/dump/f53ebccd-bc61-49f9-83db-625f209c95f5?bom=True |
| 311 2020     | https://data.boston.gov/datastore/dump/6ff6a6fd-3141-4440-a880-6f60a37fe789?bom=True |
| 311 2019     | https://data.boston.gov/datastore/dump/ea2e4696-4a2d-429c-9807-d02eb92e0222?bom=True |
| 311 2018     | https://data.boston.gov/datastore/dump/2be28d90-3a90-4af1-a3f6-f28c1e25880a?bom=True |
| 311 2017     | https://data.boston.gov/datastore/dump/30022137-709d-465e-baae-ca155b51927d?bom=True |
| 311 2016     | https://data.boston.gov/datastore/dump/b7ea6b1b-3ca4-4c5b-9713-6dc1db52379a?bom=True |
| 311 2015     | https://data.boston.gov/datastore/dump/c9509ab4-6f6d-4b97-979a-0cf2a10c922b?bom=True |


