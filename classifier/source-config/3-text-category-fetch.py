#!/usr/bin/env python3
#
# given source config, downloads data to extract text categories
#
import argparse
import os
import json
import subprocess
import csv

parser = argparse.ArgumentParser(description="generate source config java methods via openAI API")
parser.add_argument('data', type=str, help="city data subdirectory: 039-buffalo-crime")
args = parser.parse_args()

DIR=f"{args.data}"
META_FILE=f"./{DIR}/meta.json"
CSV_FILE=f"{DIR}/data-full.csv"
TEXTCAT_FILE=f"{DIR}/text_categories.txt"

# build url
def get_source_config(dataTypeParser):
    SOURCE_CONFIG_FILE = f"./{DIR}/data.csv.out.json"

    if (dataParserType == "JSON"):
        SOURCE_CONFIG_FILE = f"./{DIR}/data.json.out.json"

    with open(SOURCE_CONFIG_FILE, 'r') as file:
        source_config = json.loads(file.read())
    return source_config



with open(META_FILE, 'r') as file:
    meta = json.loads(file.read())

url = meta['url']
dataParserType = meta['dataParserType']
source_config = get_source_config(dataParserType)
report_category = source_config['mapping']['reportCategory']['field']

#
# download
# filter report category and/or save to text_cat
# json sort/uniq done via query
#
if (dataParserType == "JSON"):
    url += f"?$select={report_category}&$group={report_category}&$limit=100000"
    command = f"curl '{url}' | jq -r '.[].{report_category}' > {TEXTCAT_FILE}"
    print(f"downloading {url}")
    subprocess.run(command, shell=True, check=True)

if (dataParserType == "CSV"):
    command = f"curl -C - '{url}' > {CSV_FILE}"
    print(f"downloading {url}")
    #subprocess.run(command, shell=True, check=True)

    print("filter unique and sort")
    unique_items = set()
    with open(CSV_FILE, mode='r', newline='') as infile:
        reader = csv.DictReader(infile)
        for row in reader:
            unique_items.add(row[report_category])

    sorted_items = sorted(unique_items)

    with open(TEXTCAT_FILE, mode='w') as outfile:
        for item in sorted_items:
            outfile.write(item + '\n')



print(TEXTCAT_FILE)
print("DONE")
