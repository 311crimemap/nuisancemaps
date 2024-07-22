#!/usr/bin/env python3
#
# python classifier 311 dallas
# python classifier crime dallas
#

import argparse

parser = argparse.ArgumentParser(description="zero-shot classifier via openAI API")
parser.add_argument('type', type=str, help="data type: '311' or 'crime'")
parser.add_argument('city', type=str, help='data/<city>: subdirectory for data')
args = parser.parse_args()

DIR  = f"data/{args.city}"
INPUT_FILE=f"{DIR}/data_{type}.txt"
PROMPT_FILE="config/prompt_{type}.txt"
CATEGORY_FILE="config/categories_{type}.txt"
OUTPUT_FILE=f"{DIR}/out_{type}.json"
BATCH_SIZE=100
OFFSET=0

#PROMPT=$(<$PROMPT_FILE)
#CATEGORIES=$(awk '{printf "%s\\n", $0}' $CATEGORY_FILE) # each line, print and add newline
#DATA=$(tail -n +$OFFSET $INPUT_FILE | head -n $LIMIT | awk '{printf "%s\\n", $0}')
import os
import json
from openai import OpenAI


with open(INPUT_FILE, 'r') as file:
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

    res = json.loads(chat_completion.choices[0].message.content)['examples']
    res = [{"text": r['text'].strip(), "index": r['index']} for r in res]
    results += res


with open(OUTPUT_FILE, 'w') as file:
    json.dump(results, file)

file.close()

print(f"{OUTPUT_FILE}: {len(results)}/{len(text_categories)}")
