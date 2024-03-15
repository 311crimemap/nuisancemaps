from datasets import load_dataset

dataset = load_dataset("yelp_review_full")

model = "distilbert-base-uncased"


# DATA / TOKEN

from transformers import AutoTokenizer

tokenizer = AutoTokenizer.from_pretrained(model)


def tokenize_function(examples):
    return tokenizer(examples["text"], padding="max_length", truncation=True)


tokenized_datasets = dataset.map(tokenize_function, batched=True)


small_train_dataset = tokenized_datasets["train"].shuffle(seed=42).select(range(100))
small_eval_dataset = tokenized_datasets["test"].shuffle(seed=42).select(range(100))

# TRAIN

from transformers import AutoModelForSequenceClassification

model = AutoModelForSequenceClassification.from_pretrained(model, num_labels=5)

from transformers import TrainingArguments

training_args = TrainingArguments(output_dir="test_trainer")


# EVALUATE
import numpy as np
import evaluate

metric = evaluate.load("accuracy")

def compute_metrics(eval_pred):
    logits, labels = eval_pred
    predictions = np.argmax(logits, axis=-1)
    return metric.compute(predictions=predictions, references=labels)



# TRAIN
from transformers import TrainingArguments, Trainer

training_args = TrainingArguments(output_dir="test_trainer", evaluation_strategy="epoch")

trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=small_train_dataset,
    eval_dataset=small_eval_dataset,
    compute_metrics=compute_metrics,
    tokenizer=tokenizer
)

trainer.train()

model.save_pretrained("fine_tuned_model")
