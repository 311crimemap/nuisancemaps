#!/usr/bin/env python3
#
# python convert_csv_to_json dallas

import os
import csv
import json
import argparse

parser = argparse.ArgumentParser(description="convert csv output to json object for api submission")
parser.add_argument('data', type=str, help="city data subdirectory: 039-buffalo-crime")
args = parser.parse_args()

DIR=f"{args.data}"
TEXTCAT_FILE=f"./{DIR}/text_categories.txt.out.csv"
OUTPUT_FILE=f"{TEXTCAT_FILE}.final.json"

COLUMNS=['dataType', 'text', 'label']

def convert(csv_file, json_file):
    json_array = []
    # Read the CSV file
    with open(csv_file, mode='r', newline='') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:

            # ensure verification step
            for col in COLUMNS:
                if col not in row:
                    print(f"ERR missing column: {col}")
                    exit(1)

            json_array.append(row)

    # Write the JSON data to a file
    with open(json_file, mode='w') as file:
        json.dump(json_array, file, indent=4)



convert(TEXTCAT_FILE, OUTPUT_FILE)

print(OUTPUT_FILE)
print("DONE")
