# Classifier

Currently using ChatGPT API as a "zero-shot" classifier. Hugging Face models are
poor on 311 data - because the data is very 'vague' and requires context).

Hugging Face Slow enough that it's not worth spinning up and provisioning an
entire GPU instance for a helper utility.


All data is in `/data/<city>`.

## Steps

#### `0-data.py`

1. Download data

* `curl <url>?$query=select distinct <field>... > data/<city>/data_311.json`
* `curl <url>?$query=select distinct <field>... > data/<city>/data_crime.json`

2. Output text categories to text file

* `cat /data/<city>/data_311.json | jq -r '.[].<field>' > data/<city>/data_311.txt`
* `cat /data/<city>/data_crime.json | jq -r'.[].<field>' > data/<city>/data_crime.txt`

#### `1-classifier.py <type> <city>`

sends text list to openAI for labeling

#### `2-convert-out_to_csv.py`

* Avoid copy paste, convert api json to csv, open file directly in excel.
  * need to preserve text formatting, as there's all sorts of hidden / garbage
    text that needs to be properly mapped.

* Correct any labels, add SKIP, etc and save as `labeled_311.csv`, `labeled_crime.csv`.

* Make sure `dataType`, `text`, `label` are the columns

#### `3-convert_csv_to_json.py <city>`

* converts previously saved `csv` to `json` for submission to API

#### Submit to API /textcategories endpoing

* `curl -X POST -H 'content-type: application/json' H 'X-API-KEY: ...' -d @labeled_311.json localhost:8080/textcategories`

---

## ChatGPT Classifier

Used for labeling `TextCategory` records: these are the data's <reportCategory>
that are labeled and mapped to one of several `Category` records.

1. Make sure `OPENAI_API_KEY` is set in env.
2. Download "select distinct <reportCategory> list of fields > (`data_311.json`,
   `data_crime.json`)
3. Populate `data_311.txt`, `data_crime.txt` with `TextCategory` to be
   classified (use `jq`)
4. Ensure `prompt_311.txt`, `prompt_crime.txt`, and `categories_311.txt`,
   `categories_crime.txt` are valid inputs (should not change after a while0)
5. Run `classifier.py` in docker container
6. Check `out_311.json` and `out_crime.json` for label

---

## Adding a Source

1. Populate new entity modeled on `api/src/main/resources/data/source_config.json` (but don't save)

* URL
* lat/lng center point for city
* Mappings: make sure to use JSON path notation (`/` for object depth)
* numRecords: `<URL>?$query=SELECT%20count(*)`

2. Submit json object at endpoints `/sources` or `/sources/batch`:

`curl -X POST -H 'X-API-KEY: <key>' -H 'content-type: application/json' -d @source.json localhost:8080/sources/batch`


## Adding Source's TextCategory

General process:

1. compile and download distinct `TextCategory` elements via `reportCategory` field
2. Classify, using openAI or whatever classifier

#### Getting `reportCategory` Data

Download

* `curl <url>$query=SELECT distinct <reportCategory> limit 10000 > data_crime.json`
* `curl <url>$query=SELECT distinct <reportCategory> limit 10000 > data_311.json`

Extract via jq (Note the raw output (-r) to strip quotes.)

* `cat data_crime.json | jq -r '.[].<reportCategory>' > data_crime.txt`
* `cat data_311.json | jq -r '.[].<reportCategory>' > data_311.txt`



#### /Classifier

* Current category labels are in `categories_311.txt`, and
  `categories_crime.txt`
  * (these are setup for huggingface models and scripts adapts to openAI as
    well)
* Ensure "select distinct `reportCategory`" data from query is extracted to
  `/classifier/data/<city>/data_311.txt` and `/classifier/data/<city>/data_crime.txt`. (copy paste
  lists)
* Verify prompts in `/classifier/prompt_311.txt`, `/classifier/prompt_crime.txt`
* `classifer_311.py`, `classifer_crime.py`: are setup to submit prompt, category and data to OpenAI
* output: `out_311.json`, `out_crime.json`


#### Labeling

* Classifier is a starting point, still have to manually assign labels. Easiest
to do side-by-side in Excel.

* There are some convenience scripts above, but generally the process is below

* Review labels, make sure to overwrite with any _SKIP_..

`cat out_crime.json | jq -r '.[].text'`
`cat out_crime.json | jq -r '.[].index`

* Paste into spreadsheet and modify/verify.

* Extract as csv: Title columns, save as .csv -> `labeled_crime.csv`, `labeled_311.csv`.
  * column titles: ["dataType", "text", "label"]

NB: Data Submission Format:

```
    [
...,
{
    "dataType": "crime",
    "text": <textCategory>,
    "label": 1
},
...]
```


#### Submitting new Source TextCategory(ies): convert_csv_to_json.py

`convert_csv_to_json.py <city>`: to take csv and convert to list of json for  `/textcategory` submission.

* `data/<city>/labeled_crime.csv` -> `data/<city>/labeled_crime.json`
* `data/<city>/labeled_311.csv` -> `data/<city>/labeled_311.json`


Submission:

```
* `curl -X POST -d @labeled_311.json -H 'content-type: application/json' -H 'X-API-KEY: <KEY>' localhost:8080/textcategories`
* `curl -X POST -d @labeled_crime.json -H 'content-type: application/json' -H 'X-API-KEY: <KEY>' localhost:8080/textcategories`

```


## Adding Error TextCategories

Reminder many error messages will end up being duplicates, so it's less intimidating than it looks.

NB: if getting parse errors, proper json has no dangling ','.


* Collect errors: `curl -H 'X-API-KEY:1234' localhost:8080/dataerrors | jq '.[].errorMsg`
  * these should be "missing category"
* Take each type, category and start label process for submission
* Either manually label category, or submit to openAI

Manual Example
* `labeled_crime.json` -> copy to `missing_crime.json`, for example: add
  category and label (and dataType) and submit.

OpenAI Crime Example
* add to `data_crime.txt`
* `classifier.py`
* review `out_crime.json` (jq to excel) -> `labeled_crime.csv`
* `convert_csv_to_json.py` -> `labeled_crime.json`

Submit

`curl -X POST -d @labeled_crime.json -H 'content-type: application/json' -H 'X-API-KEY: <KEY>' localhost:8080/textcategories`
