#!/usr/bin/env/ python3

#
# takes $DIR/data.csv and prints output form assign fields for quick verification
#

import argparse
import os
import json
import subprocess

parser = argparse.ArgumentParser(description="generate source config java methods via openAI API")
parser.add_argument('data', type=str, help="city data subdirectory: 039-buffalo-crime")
args = parser.parse_args()

DIR=f"{args.data}"
META_FILE = f"./{DIR}/meta.json"
SOURCE_CONFIG_FILE = f"./{DIR}/source_config.json"

with open(SOURCE_CONFIG_FILE, 'r') as file:
    source_config = json.loads(file.read())

mapping = source_config['mapping']
dataParserDelimeter = mapping.get('dataParserDelimeter', ",")
dataParserType = source_config['dataParserType']

fields = [
    'reportNum',
    'reportCategory',
    'description',
    'address',
    'location',
    'latitude',
    'longitude',
    'reportedAt',
    'reportedAt2'
]

if (dataParserType == 'JSON'):
    mappedFields = [mapping[field]['field'] for field in fields if mapping[field]['field']]
    jsonFields = "{" + ",".join(mappedFields) + "}"
    command = f"jq 'map({jsonFields})' {DIR}/data.json"

else:
    mappedFields = [mapping[field]['field'] for field in fields if mapping[field]['field']]
    csvFields = ",".join(mappedFields)
    command = f"csvcut -d '{dataParserDelimeter}' -c '{csvFields}' {DIR}/data.csv | csvlook -"


result = subprocess.run(command, shell=True, check=True)
