#!/usr/bin/bash

DIR=$1

if [ -z "$DIR" ]; then
    echo "missing arg directory"
    echo "./generate.sh data/042-test-crime-2024"
    exit 1
fi

python fields.py $DIR

python 3-text-category-fetch.py $DIR

python 4-text-category-classifier.py $DIR

python 5-text-category-to-json-for-submit.py $DIR
