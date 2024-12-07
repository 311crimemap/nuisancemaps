#!/usr/bin/env python3

import argparse
import os
import json
import subprocess

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

if (command is None):
    print("[ERROR] meta.json: dataParserType unrecognized")
    exit(1)

subprocess.run(command, shell=True, check=True)

print(META_FILE)
print("DONE")
