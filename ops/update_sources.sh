#!/usr/bin/bash
source '../.env'

API_HOST=https://api.311crimemap.com
IDS=(
    2  # austin police incident report
    3  # austin 311
    4  # dallas police
    5  # dallas 311
    6  # chicago crime
    7  # chicago 311
    8  # sf crime
    9  # sf 311
    11 # nyc ytd
    13 # boston crime 2023 to present
    22 # boston 2024 ->
    # 12 # nyc 311 (large file) check for free space
)
for source_id in "${IDS[@]}"
do
    echo "Requesting source_id: $source_id"

    curl -X POST \
         -H 'content-type:application/json' \
         -H "X-API-KEY: $ADMIN_API_KEY" \
         $API_HOST/datajobs/sources/$source_id

    sleep 1
    echo "\n"
done

