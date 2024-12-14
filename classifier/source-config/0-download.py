#!/usr/bin/env python3

import argparse
import os
import json
import subprocess
import pandas as pd

parser = argparse.ArgumentParser(description="generate source config java methods via openAI API")
parser.add_argument('data', type=str, help="city data subdirectory: 039-buffalo-crime")
args = parser.parse_args()

DIR=f"{args.data}"
META_FILE=f"./{args.data}/meta.json"


with open(META_FILE, 'r') as file:
    meta = json.loads(file.read())

url = meta['url']


command = None

if (meta['dataParserType'] == 'JSON'):
    command = f"curl '{url}?$limit=3' > {DIR}/data.json"

if (meta['dataParserType'] == 'CSV'):
    command = f"curl '{url}' | awk 'NR > 3 {{ exit }} {{ print}}' > {DIR}/data.csv"

if (meta['dataParserType'] == 'XLS'):
    command = f"curl '{url}' > {DIR}/data.xls"

if (command is None):
    print("[ERROR] meta.json: dataParserType unrecognized")
    exit(1)

subprocess.run(command, shell=True, check=True)

# convert xls to csv
if (meta['dataParserType'] == 'XLS'):
    print("Converting .xls to .csv")
    print("Reading xls")
    df = pd.read_excel(f"{DIR}/data.xls")
    print("Truncate xls")
    truncated_df = df.head(5)
    truncated_df.to_csv(f"{DIR}/data.csv", index=False)


print(META_FILE)
print("DONE")
