#!/usr/bin/bash
#
# see PRODUCTION.md
#

echo "Did you mean update_sources.py"
exit

source '../.env'

API_HOST=https://api.311crimemap.com
IDS=(
    2
    35
    36
    # 37 # dallas 311 replace
    38
    34
    186
    32
    39
    33
    13
    217
    187
    206
    216
    191
    229
    230
    99
    218
    105
    106
    107
    108
    109
    110
    113
    215
    117
    118
    120
    222
    124
    125
    128
    201
    202
    225
    140
    143
    226
    207
    223
    209
    221
    219
    220
    188
    172
    175
    176
    180
    181
    184
    185
    190
    227
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

