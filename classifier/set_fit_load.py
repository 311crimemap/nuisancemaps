from setfit import SetFitModel, Trainer, TrainingArguments, sample_dataset
from datasets import load_dataset, Dataset
import pandas as pd
import json

json_file = open("./classifier_categories.json")
categories = json.load(json_file)
candidate_labels = [category['text'] for category in categories['crime']]

model = SetFitModel.from_pretrained("models/setfit-bge-small-v1.5-sst2-8-shot-aws") # Load from a local directory

# Performing inference
json_file = open("./data/test-crime.json")
data = json.load(json_file)
inputs = [d['text'] for d in data]

preds = model.predict(inputs)

matches = 0
index = 0
for pred in preds:
    matches += (1 if data[index]['label'] == candidate_labels.index(pred) else 0)
    index += 1

print ("Accuracy: ", matches / len(preds))

#print(preds)
#[print(candidate_labels.index(r)+1) for r in preds]
