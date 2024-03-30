#!/usr/bin/bash
#
# keep input data < ``00 lines
#
set -x

INPUT_FILE="data_crime.txt"
PROMPT_FILE="prompt_crime.txt"
CATEGORY_FILE="categories_crime.txt"
OUTPUT_FILE="out_crime.json"
LIMIT=100
OFFSET=0  #augment steps of LIMIT+1: 0, 101, 202,

PROMPT=$(<$PROMPT_FILE)
CATEGORIES=$(awk '{printf "%s\\n", $0}' $CATEGORY_FILE) # each line, print and add newline
DATA=$(tail -n +$OFFSET $INPUT_FILE | head -n $LIMIT | awk '{printf "%s\\n", $0}')

curl https://api.openai.com/v1/chat/completions \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer $OPENAI_API_KEY" \
     -d "{
  \"model\": \"gpt-3.5-turbo\",
  \"messages\": [
    {
      \"role\": \"system\",
      \"content\": \"${PROMPT}\\n\\n${CATEGORIES}\"
    },
    {
      \"role\": \"user\",
      \"content\": \"${DATA}\\n\"
    }
  ],
  \"temperature\": 0,
  \"max_tokens\": 4095,
  \"top_p\": 1,
  \"frequency_penalty\": 0,
  \"presence_penalty\": 0
}" > $OUTPUT_FILE


cat $OUTPUT_FILE | jq -r '.choices[0].message.content'

# output original vs results num
echo "orig:  $(wc $INPUT_FILE)"
echo "result: $(cat $OUTPUT_FILE | jq -r '.choices[0].message.content' | jq '.examples[].index' | wc)"

# get text
#cat $OUTPUT_FILE | jq -r '.choices[0].message.content' | jq '.examples[].index'

# get labels
#cat $OUTPUT_FILE | jq -r '.choices[0].message.content' | jq '.examples[].text'
