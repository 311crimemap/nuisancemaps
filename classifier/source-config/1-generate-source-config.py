#!/usr/bin/env python3

import argparse

parser = argparse.ArgumentParser(description="generate source-config via openAI API")
parser.add_argument('data', type=str, help="city data subdirectory: 039-buffalo-crime")
args = parser.parse_args()

DIR=f"{args.data}"
META_FILE = f"./{DIR}/meta.json"
PROMPT_FILE=f"prompt/1-gen-source-config.txt"
BATCH_SIZE=10
OFFSET=0

import os
import json
from openai import OpenAI

with open(PROMPT_FILE, 'r') as file:
    prompt = file.read()

with open(META_FILE, 'r') as file:
    meta = json.loads(file.read())

DATA_FILE=f"./{DIR}/data.json"
if (meta['dataParserType'] in ["CSV", "XLS"]):
    DATA_FILE=f"./{DIR}/data.csv"

OUTPUT_FILE=f"./{DATA_FILE}.out.json"
with open(DATA_FILE, 'r') as file:
    data = file.read()


client = OpenAI()

print(f"Requesting")

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
if (DATA_FILE.endswith("csv")):
    mapping = res['mapping']
    for (k,v) in mapping.items():
        if "pointer" in v:
            v['pointer'] = None

# res: add meta (maintain ordering)
res = {**meta, **res}

# write total output per iteration in case of error
with open(OUTPUT_FILE, 'w') as file:
    json.dump(res, file)

    file.close()



print(OUTPUT_FILE)
print("DONE")
