#!/usr/bin/env python3

import argparse

parser = argparse.ArgumentParser(description="zero-shot classifier via openAI API")
parser.add_argument('catalog', type=str, help="catalog file: 'catalog-filtered.json'")
args = parser.parse_args()


CATALOG_FILE=f"./{args.catalog}"
PROMPT_FILE=f"config/prompt.txt"
OUTPUT_FILE=f"./out.json"
BATCH_SIZE=10
OFFSET=0

import os
import json
from openai import OpenAI

with open(CATALOG_FILE, 'r') as file:
    catalog = json.load(file)

with open(PROMPT_FILE, 'r') as file:
    prompt = file.read()

results = {}

client = OpenAI()
while (OFFSET < len(catalog)):
    print(f"Requesting {OFFSET}:{OFFSET + BATCH_SIZE} / {len(catalog)}")
    data = catalog[OFFSET : OFFSET + BATCH_SIZE]

    OFFSET += BATCH_SIZE

    chat_completion = client.chat.completions.create(
        messages=[
            {
                "role": "system",
                "content": f"{prompt}"
            },
            {
                "role": "user",
                "content": f"{data}",
            }
        ],

        response_format={
            "type": "json_object",
        },

        model = "gpt-4o-mini",

        temperature = 0,
        #max_tokens = 4095,
        top_p = 0.0,
        frequency_penalty = 0,
        presence_penalty = 0
    )


    res = json.loads(chat_completion.choices[0].message.content)['results']

    for r in res:
        results[ r['resource_id'] ] = r
    print(res)
    # [k for (k,v) in results.items() if v == True]

    # write total output per iteration in case of error
    with open(OUTPUT_FILE, 'w') as file:
        json.dump(results, file)

    file.close()



print(f"{OUTPUT_FILE}: {len(results)}/{len(catalog)}")

print("DONE")
