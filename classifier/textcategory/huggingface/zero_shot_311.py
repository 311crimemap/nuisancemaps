import json
from transformers import pipeline

#
# on train-311.json:
#
# accuracy: 44.88%
# slow: even wth gpu still takes ~ 30 secs for examples
#
json_file = open("./classifier_categories.json")
categories = json.load(json_file)

candidate_labels = []
for headCategory in categories['311']:
    for category in headCategory['subcategories']:
        candidate_labels.append( category['text'] )


# classifier
classifier = pipeline("zero-shot-classification",
                      model="facebook/bart-large-mnli", device=0)

json_file = open("./data/train-311.json")
all_data = json.load(json_file)
#tex/tlabel
i = 0
match = 0
_all = [ d['text'] for d in all_data ]
results = classifier(_all, candidate_labels)
for res in results:
    dlabel = candidate_labels[ all_data[i]['label'] ]
    print(res['sequence'], "|", res['labels'][0], "|", dlabel, "|", res['labels'][0] == dlabel)
    match += 1 if res['labels'][0] == dlabel else 0
    i+=1

print("ACC:", match / len(all_data) )
print("DONE")
