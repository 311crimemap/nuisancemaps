#!/usr/bin/env python3

import argparse
import os
import json
from openai import OpenAI

parser = argparse.ArgumentParser(description="generate source config java methods via openAI API")
parser.add_argument('data', type=str, help="city data subdirectory: 039-buffalo-crime")
args = parser.parse_args()

DIR=f"{args.data}"
META_FILE = f"./{DIR}/meta.json"
PROMPT_FILE=f"prompt/2-generate-source-methods.txt"

with open(PROMPT_FILE, 'r') as file:
    prompt = file.read()

with open(META_FILE, 'r') as file:
    meta = json.loads(file.read())

DATA_FILE=f"./{DIR}/data.json"
if (meta['dataParserType'] == "CSV"):
    DATA_FILE=f"./{DIR}/data.csv"

with open(DATA_FILE, 'r') as file:
    data = file.read()

SOURCE_CONFIG_FILE=f"./{DATA_FILE}.out.json"
with open(SOURCE_CONFIG_FILE, 'r') as file:
    source_config = file.read()

OUTPUT_FILE=f"{SOURCE_CONFIG_FILE}.methods.txt"


data = f"""
TEMPLATE:
{source_config}

---

DATA:
{data}
"""

print(data)

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
        "type": "text",
    },

    model = "gpt-4o-mini",

    temperature = 0,
    #max_tokens = 4095,
    top_p = 0.0,
    frequency_penalty = 0,
    presence_penalty = 0
)


res = chat_completion.choices[0].message.content
print(res)

# write total output per iteration in case of error
with open(OUTPUT_FILE, 'w') as file:
    file.write(res)
    file.close()



print(OUTPUT_FILE)
print("DONE")
