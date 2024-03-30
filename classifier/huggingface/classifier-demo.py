import numpy as np
import json
from transformers import AutoTokenizer
from datasets import load_dataset, Dataset

json_file = open("./classifier_categories.json")
categories = json.load(json_file)

candidate_labels = []
for headCategory in categories['311']:
    for category in headCategory['subcategories']:
        candidate_labels.append( category['text'] )

#
# example data format:
# all_data = [ { "text": "crime", "label": 6}, ...]
#
#
json_file = open("./data/train-311.json")
all_data = json.load(json_file)

# Preprocess
datasets = Dataset.from_list(all_data).shuffle(seed=42)\
                  .class_encode_column("label")\
                  .train_test_split(test_size=0.20, stratify_by_column="label")

tokenizer = AutoTokenizer.from_pretrained("distilbert/distilbert-base-uncased")

def preprocess_function(examples):
    return tokenizer(examples["text"], truncation=True)

tokenized_data = datasets.map(preprocess_function, batched=True)


from transformers import DataCollatorWithPadding

data_collator = DataCollatorWithPadding(tokenizer=tokenizer)



# eval
import evaluate
accuracy = evaluate.load("accuracy")

def compute_metrics(eval_pred):
    predictions, labels = eval_pred
    predictions = np.argmax(predictions, axis=1)
    return accuracy.compute(predictions=predictions, references=labels)

# TRAIN

id2label ={ }
label2id = {}
for cl in enumerate(candidate_labels):
    id2label[cl[0]] = cl[1]
    label2id[cl[1]] = cl[0]

# Preparing the dataset
    
from transformers import AutoModelForSequenceClassification, TrainingArguments, Trainer

model = AutoModelForSequenceClassification.from_pretrained(
    "distilbert/distilbert-base-uncased",
    num_labels=len(candidate_labels),
    id2label=id2label, label2id=label2id
)    

training_args = TrainingArguments(
    output_dir="my_model",
    learning_rate=2e-5,
    per_device_train_batch_size=16,
    per_device_eval_batch_size=16,
    num_train_epochs=100,
    weight_decay=0.01,    
    evaluation_strategy="epoch",
    #save_strategy="epoch",
    #load_best_model_at_end=True,
    #push_to_hub=False,
)

trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=tokenized_data["train"],
    eval_dataset=tokenized_data["test"],
    tokenizer=tokenizer,
    data_collator=data_collator,
    compute_metrics=compute_metrics,
)

trainer.train()

trainer.save_model()
# inference check

tests = [
    "Animal - Proper Care",
    "APH - Graffiti Abatement - Public Property",
    "Parking Ticket Complaint",
    "WPD - Standing Water",
]


from transformers import pipeline

classifier = pipeline("text-classification", model="my_model")
res = classifier(tests)
print(res)



# acuracy loop


json_file = open("./data/test-311.json")
data = json.load(json_file)
inputs = [d['text'] for d in data]
preds = classifier(inputs)

matches = 0
index = 0
for pred in preds:
    pred_label = pred['label']
    data_label = candidate_labels[data[index]['label']]
    matches += (1 if data_label == pred_label else 0)

    #@ delimiter
    print(data[index]['text'], "@", data_label, "|", pred_label,"|", data_label==pred_label)

    index += 1


print ("Accuracy: ", matches / len(preds))
