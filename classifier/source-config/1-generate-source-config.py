#!/usr/bin/env python3

import argparse

parser = argparse.ArgumentParser(description="generate source-config via openAI API")
parser.add_argument('input', type=str, help="same input file: '41.json'")
args = parser.parse_args()


CATALOG_FILE=f"./{args.input}"
PROMPT_FILE=f"prompt/1-gen-source-config.txt"
OUTPUT_FILE=f"./{args.input}.out.json"
BATCH_SIZE=10
OFFSET=0

import os
import json
from openai import OpenAI

with open(CATALOG_FILE, 'r') as file:
    catalog = file.read()

with open(PROMPT_FILE, 'r') as file:
    prompt = file.read()

client = OpenAI()

print(f"Requesting")
data = catalog

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


res = json.loads(chat_completion.choices[0].message.content)
print(res)

# clear out pointer if csvfile
if (CATALOG_FILE.endswith("csv")):
    mapping = res['mapping']
    for (k,v) in mapping.items():
        if "pointer" in v:
            v['pointer'] = None


# write total output per iteration in case of error
with open(OUTPUT_FILE, 'w') as file:
    json.dump(res, file)

    file.close()



print(f"{OUTPUT_FILE}")

print("DONE")
