from transformers import AutoModelForSequenceClassification, AutoTokenizer
model = AutoModelForSequenceClassification.from_pretrained("fine_tuned_model", from_tf=False)

tokenizer = AutoTokenizer.from_pretrained('distilbert-base-uncased')

from transformers import pipeline
clf = pipeline("text-classification", model, tokenizer=tokenizer)

#answer = clf("text", return_all_scores=True)
