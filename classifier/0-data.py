#!/usr/bin/env python

print("Step 1: Get TextCategory data: curl `<datasource>?$query=select distinct <column> > data_<type>.json`")
print("")
print("Step 2: Filter json to txt: `cat data_<type>.json | 'jq -r '.[].<col> > data_<type>.txt`")
print("")
print("Step 3: Run 1-classifier to send txt to open ai for labeling")
