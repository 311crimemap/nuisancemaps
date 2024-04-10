#!/usr/bin/bash
set -x

HOST="localhost:8080"

#
# seed categories
#

curl -H 'content-type:application/json' \
     -X POST \
     -d @api/src/main/resources/data/classifier_categories.json \
     $HOST/categories

#
# Seed Source and Mappings
#


curl -H 'content-type:application/json' \
     -X POST \
     -d @api/src/main/resources/data/source_config.json \
     $HOST/sources/batch
