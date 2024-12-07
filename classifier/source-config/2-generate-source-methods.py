#!/usr/bin/env python3

import argparse

parser = argparse.ArgumentParser(description="generate source config java methods via openAI API")
#parser.add_argument('source_config', type=str, help="source config generated file: 'buffalo.json.out.json'")
parser.add_argument('data', type=str, help="city data subdirectory: 039-buffalo-crime")
parser.add_argument('input', type=str, help="input file of records (csv or json): 'buffalo.json'")
args = parser.parse_args()

DIR=f"{args.data}"
SAMPLE_DATA_FILE=f"./{args.input}"
SOURCE_CONFIG_FILE=f"./{args.input}.out.json"
PROMPT_FILE=f"prompt/2-generate-source-methods.txt"
OUTPUT_FILE=f"{SOURCE_CONFIG_FILE}.methods.txt"

import os
import json
from openai import OpenAI

with open(SOURCE_CONFIG_FILE, 'r') as file:
    source_config = file.read()

with open(SAMPLE_DATA_FILE, 'r') as file:
    sample_data = file.read()

with open(PROMPT_FILE, 'r') as file:
    prompt = file.read()

data = f"""
TEMPLATE:
{source_config}

---

DATA:
{sample_data}
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



print(f"{OUTPUT_FILE}")

print("DONE")
