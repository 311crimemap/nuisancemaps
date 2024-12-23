#!/usr/bin/env python3
#
# given source config, downloads data to extract text categories
#
import sys
import argparse
import os
import json
import subprocess
import csv
import itertools
import chardet
import pandas as pd

parser = argparse.ArgumentParser(description="generate source config java methods via openAI API")
parser.add_argument('data', type=str, help="city data subdirectory: 039-buffalo-crime")
args = parser.parse_args()

DIR=f"{args.data}"
META_FILE=f"./{DIR}/meta.json"
CSV_FILE=f"{DIR}/data-full.csv"
TEXTCAT_FILE=f"{DIR}/text_categories.txt"

# build url
def get_source_config(dataTypeParser):
    SOURCE_CONFIG_FILE = f"./{DIR}/source_config.json"

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
mapping = source_config['mapping']
dataParserDelimeter = mapping.get("dataParserDelimeter", ",")
dataParserNumSkip = mapping.get("dataParserNumSkip", 0)

# categories
report_category = mapping['reportCategory']['field']
latitude_category = mapping['latitude']['field']
longitude_category = mapping['longitude']['field']

csv.field_size_limit(sys.maxsize)
#
# download
# filter report category and/or save to text_cat
# json sort/uniq done via query
#
if (dataParserType == "JSON"):
    # TODO: filter out report_category missing lat/lng
    url += f"?$select={report_category}&$group={report_category}&$limit=100000"
    command = f"curl '{url}' | jq -r '.[].{report_category}' > {TEXTCAT_FILE}"
    print(f"downloading {url}")
    subprocess.run(command, shell=True, check=True)

if (dataParserType == "XLS"):
    print("Converting .xls to .csv")
    print("Reading xls")
    df = pd.read_excel(f"{DIR}/data.xls")

    # TODO: verify
    filtered_df = df[df[latitude_category].notna() & df[longitude_category].notna()]
    print("filter unique and sort")
    sorted_items = sorted(filtered_df[report_category].dropna().unique())
    print("Example Categories:", sorted_items[0:5])

    with open(TEXTCAT_FILE, mode='w') as outfile:
        for item in sorted_items:
            outfile.write(item + '\n')


if (dataParserType in ["CSV", "CSVCUSTOM"]):
    command = f"curl -L -C - '{url}' > {CSV_FILE}"
    print(f"downloading {url}")
    subprocess.run(command, shell=True, check=True)

    # grab encoding
    with open(CSV_FILE, 'rb') as rawfile:
        print("Detecting encoding")
        chunk = rawfile.read(1024 * 1024)
        result = chardet.detect(chunk)
        encoding = result['encoding']
        print(f"Detected chardet: {encoding}")
        #encoding = "utf-8"
        encoding = encoding.lower() if encoding.lower() in ["utf-8", "utf-8-sig"] else "latin1"
        print(f"Using Encoding: {encoding}")

    print("filter unique and sort")
    unique_items = set()
    numRow = 0
    with open(CSV_FILE, mode='r', newline='', encoding=encoding) as infile:

        reader = csv.DictReader(itertools.islice(infile, dataParserNumSkip, None),  delimiter=dataParserDelimeter)
        print(f"Skipping {dataParserNumSkip} lines")

        for row in reader:
            try:
                # check if lat/lng exist - if they don't, we'll skip it in parse
                # so don't bother extracting (reduces a lot of 'noisy' online only submissions)
                lat = row[latitude_category]
                lng = row[longitude_category]

                if not (lat and lat.strip() and lng and lng.strip()):
                    continue

                val = row[report_category]
                if (val and val.strip()):
                    unique_items.add(val)
            except Exception as e:
                print(f"[ERR] {e}")

    sorted_items = sorted(unique_items)
    print("Example Categories:", sorted_items[0:5])

    with open(TEXTCAT_FILE, mode='w') as outfile:
        for item in sorted_items:
            outfile.write(item + '\n')



print(TEXTCAT_FILE)
print("DONE")
