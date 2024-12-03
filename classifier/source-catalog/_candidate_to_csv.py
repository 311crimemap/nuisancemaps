#!/usr/bin/env python3

import os
import json
import csv

CATALOG_FILE=f"./catalog-filtered.json"
OPEN_AI_FILE=f"./out.json"

# build catalog map
with open(CATALOG_FILE, 'r') as file:
    catalog = json.load(file)

catalog_map = {}
for resource in catalog:
    r= resource['resource']
    catalog_map[ r['id'] ] = r


# load open ai results
with open(OPEN_AI_FILE, 'r') as file:
    matches = json.load(file)

# filter
crime = {}
_311 = {}

for (k,v) in matches.items():
    if (v['type'] == "crime"):
        #print(catalog_map[k]['name'])
        crime[k] = catalog_map[k]

    if (v['type'] == "311"):
        #print(catalog_map[k]['name'])
        _311[k] = catalog_map[k]

# WORKING HERE - make sure data-crime / data-311.csv contain  all the catalog fields
# then open csv to filter
crime_rows = [{"key": k, **v} for k, v in crime.items()]
_311_rows = [{"key": k, **v} for k, v in _311.items()]

with open('data-crime.csv', 'w', newline='') as csvfile:
    fieldnames = crime_rows[0].keys()
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    writer.writeheader()
    writer.writerows(crime_rows)

with open('data-311.csv', 'w', newline='') as csvfile:
    fieldnames = _311_rows[0].keys()
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    writer.writeheader()
    writer.writerows(_311_rows)

print("DONE")
