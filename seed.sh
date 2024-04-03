#!/usr/bin/bash
set -x

#
# seed categories
#

HOST="localhost:8080"

curl -H 'content-type:application/json' \
     -X POST \
     -d @api/src/main/resources/data/classifier_categories.json \
     $HOST/categories
