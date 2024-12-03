#!/usr/bin/env python3

import os
import json
import csv

#CATALOG_FILE=f"./police-crime-raw.json"
#OUT_FILE =f"./data-crime.csv"

CATALOG_FILE=f"./311-raw.json"
OUT_FILE =f"./data-311.csv"


def to_csv(filename, data_rows):
    with open(filename, 'w', newline='') as csvfile:
        fieldnames = data_rows[0].keys()
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(data_rows)

# build data from catalog
with open(CATALOG_FILE, 'r') as file:
    catalog = json.load(file)


fields = [
    'id',
    'name',
    'description',
    'attribution',
    'type',
    'updatedAt',
    'createdAt',
    'data_updated_at',
    'columns_field_name',
    'columns_description',
    'provenance',
]

#311 same

data_rows = []
for result in catalog['results']:
    row = result['resource']
    r = {key: value for key, value in row.items() if key in fields}
    data_rows.append(r)


to_csv(OUT_FILE, data_rows)
