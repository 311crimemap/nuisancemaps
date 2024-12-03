#!/usr/bin/env python
#
# usage:
# python 0-data_crime.py 07/22/2024
#
import os
import re
import json

import argparse

parser = argparse.ArgumentParser(description="utility takes data_X.json file and removes extra spaces between words")
parser.add_argument('type', type=str, help="data type: '311' or 'crime'")
parser.add_argument('city', type=str, help='data/<city>: subdirectory for data')
args = parser.parse_args()

DIR  = f"data/{args.city}"
FILENAME = f"{DIR}/labeled_{args.type}.json"

def save_file(content, filename):
    print("Saving file:", filename)
    with open(filename, 'w') as outfile:
        json.dump(content, outfile)

def read_file(filename):
    with open(filename) as file:
        vals = json.load(file)
        return vals

def replace(vals):
    for val in vals:
        text = val["text"]
        text = re.sub(r'\s+', ' ', text).strip()
        val["text"] = text

    return vals


vals = read_file(FILENAME)
vals = replace(vals)
save_file(vals, FILENAME)
