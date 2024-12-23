#!/usr/bin/bash

DIR=$1

if [ -z "$DIR" ]; then
    echo "missing arg directory"
    echo "./generate.sh data/042-test-crime-2024"
    exit 1
fi


python 0-download.py $DIR

python 1-generate-source-config.py $DIR

python 2-generate-source-methods.py $DIR

echo "copying $DIR/source_config.json"
cp $DIR/data.csv.out.json $DIR/source_config.json

