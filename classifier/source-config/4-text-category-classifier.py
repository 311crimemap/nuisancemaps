#!/usr/bin/env python3
#
# python classifier 311 dallas
# python classifier crime dallas
#

import argparse
import os
import json
import subprocess
import csv
import re 
import pandas as pd
from pandas import json_normalize
from openai import OpenAI

parser = argparse.ArgumentParser(description="zero-shot classifier via openAI API")
parser.add_argument('data', type=str, help="city data subdirectory: 039-buffalo-crime")
args = parser.parse_args()

DIR=f"{args.data}"
META_FILE=f"./{DIR}/meta.json"
TEXTCAT_FILE=f"./{DIR}/text_categories.txt"

with open(META_FILE, 'r') as file:
    meta = json.loads(file.read())
TYPE = meta['category']

PROMPT_FILE=f"prompt/prompt_{TYPE}.txt"
CATEGORY_FILE=f"prompt/categories_{TYPE}.txt"
OUTPUT_FILE=f"./{DIR}/text_categories.txt.out.json"
BATCH_SIZE=100
OFFSET=0

# clean up any malformed decoding
def format_str(content):
    # what else is there jesus
    _content = content.replace("\\uFEFF", "")\
                      .replace("\\u00A0", "")\
                      .replace("\\u200B", "")\
                      .replace("\\xa0", "")\
                      .replace('Â\\x80ï¿½', "")\
                      .replace("\x80�", "")

    re.sub(r'\\x[0-9A-Fa-f]{2}', '', _content)

    cleaned_content = _content.encode('utf-8', errors='ignore')\
                              .decode('utf-8', errors='ignore')

    return cleaned_content


with open(META_FILE, 'r') as file:
    meta = json.loads(file.read())

with open(TEXTCAT_FILE, 'r') as file:
    text_categories = file.readlines()

with open(PROMPT_FILE, 'r') as file:
    prompt = file.read()

with open(CATEGORY_FILE, 'r') as file:
    categories = file.read()

results = []

client = OpenAI()
while (OFFSET < len(text_categories)):
    print(f"Requesting {OFFSET}:{OFFSET + BATCH_SIZE} / {len(text_categories)}")
    data = text_categories[OFFSET : OFFSET + BATCH_SIZE]

    OFFSET += BATCH_SIZE

    chat_completion = client.chat.completions.create(
        messages=[
            {
                "role": "system",
                "content": f"{prompt}\n\n{categories}"
            },
            {
                "role": "user",
                "content": f"{data}",
            }
        ],
        #model = "gpt-3.5-turbo",
        model = "gpt-4o-mini",

        temperature = 0,
        #max_tokens = 4095,
        top_p = 1,
        frequency_penalty = 0,
        presence_penalty = 0
    )

    category = meta['category']
    content = format_str(chat_completion.choices[0].message.content)

    examples = []
    try:
        examples = json.loads(content)['examples']
    except Exception as e:
        print("[ERR]:", e)
        print(content)

    for example in examples:
        try:
            res = {
                "dataType": meta['category'],
                "text": example['text'].strip(),
                "label": example['index']
            }
            results.append(res)
        except Exception as e:
            print("[ERR]: ", e)
            print(example)


with open(OUTPUT_FILE, 'w') as file:
    json.dump(results, file)

file.close()

print(f"{OUTPUT_FILE}: {len(results)}/{len(text_categories)}")


# 2-convert_out_to_csv.py
# convert classified json to csv for browsing in spreadsheet

CSV_FILE = f"./{DIR}/text_categories.txt.out.csv"
df = json_normalize(results)
df.to_csv(CSV_FILE, index=False)

print(f"'open {CSV_FILE}' to verify labels and edit columns for submission")
print("NB: columns: | dataType (crime/311) | text | label |" )
print("DONE")
