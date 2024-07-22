import pandas as pd
import json
from pandas import json_normalize
import argparse

parser = argparse.ArgumentParser(description="convert out_*.json from api submission to csv")
parser.add_argument('type', type=str, help="data type: '311' or 'crime'")
parser.add_argument('city', type=str, help='data/<city>: subdirectory for data')
args = parser.parse_args()

DIR  = f"data/{args.city}"

API_FILE = f"{DIR}/out_{args.type}.json"
CSV_FILE = f"{DIR}/labeled_{args.type}.csv"

with open(API_FILE, 'r') as json_file:
    api_data = json.load(json_file)

df = json_normalize(api_data)

df.to_csv(CSV_FILE, index=False)
