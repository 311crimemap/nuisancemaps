#!/usr/bin/bash

cat $1 | jq -r '.choices[0].message.content' | jq '.examples'
