#!/usr/bin/env python3
import argparse

parser = argparse.ArgumentParser(description="convert csv output to json object for api submission")
parser.add_argument('city', type=str, help='data/<city>: subdirectory for data')
args = parser.parse_args()

DIR  = f"data/{args.city}"

CSV_FILE_CRIME = f"{DIR}/labeled_crime.csv"
CSV_FILE_311 = f"{DIR}/labeled_311.csv"

JSON_FILE_CRIME = f"{DIR}/labeled_crime.json"
JSON_FILE_311 = f"{DIR}/labeled_311.json"

import os
import csv
import json

def convert(csv_file, json_file):
    json_array = []
    # Read the CSV file
    with open(csv_file, mode='r', newline='') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            json_array.append(row)

    # Write the JSON data to a file
    with open(json_file, mode='w') as file:
        json.dump(json_array, file, indent=4)



convert(CSV_FILE_CRIME, JSON_FILE_CRIME)
convert(CSV_FILE_311, JSON_FILE_311)
