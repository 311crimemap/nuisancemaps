#!/usr/bin/bash
#
# see PRODUCTION.md
#

source '../.env'

API_HOST=https://api.311crimemap.com
IDS=(
    2
    35
    36
    37
    38
    34
    186
    32
    39
    33
    13
    200
    187
    41
    47
    191
    193
    194
    99
    192
    105
    106
    107
    108
    109
    110
    113
    114
    117
    118
    120
    123
    124
    125
    128
    129
    134
    139
    140
    143
    144
    145
    149
    154
    155
    160
    165
    188
    172
    175
    176
    180
    181
    184
    185
    190
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

