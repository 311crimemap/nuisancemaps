# Classifier

Currently using ChatGPT API as a "zero-shot" classifier. Hugging Face models are
poor on 311 data - because the data is very 'vague' and requires context).

Hugging Face Slow enough that it's not worth spinning up and provisioning an
entire GPU instance for a helper utility.


All data is in `/data/<city>`.

## Steps

1. Download data
* `curl <url>?$query=select distinct <field>... > data/<city>/data_311.json`
* `curl <url>?$query=select distinct <field>... > data/<city>/data_crime.json`

2. cat /data/<city>/data_311.json | jq '.[].text'


## ChatGPT Classifier

1. Make sure `OPENAI_API_KEY` is set in env.
2. Download "select distinct <reportCategory> list of fields > (`data_311.json`,
   `data_crime.json`)
3. Populate `data_311.txt`, `data_crime.txt` with `TextCategory` to be
   classified (use `jq`)
4. Ensure `prompt_311.txt`, `prompt_crime.txt`, and `categories_311.txt`,
   `categories_crime.txt` are valid inputs (should not change after a while0)
5. Run `classifier.py` in docker container
6. Check `out_311.json` and `out_crime.json` for label

## Labeling

Go through list of `textCateogry` and assign the labels, making sure to SKIP if
necessary. This isn't automatic process, there will be some mis-classification.

Consider this a starting point to help sift through everything quickly.


---

_Below is deprecated_


Bash scripts are basically copies with variables to 311 and crime related txt
files.

Adjust the `LIMIT` and `OFFSET` variables in the script to run multiple times.
Make sure preserve `out_X.json`.

* `./openai_311.sh`
  * uses `prompt_311.txt`, `categories_311.txt`
  * modify `data_311.txt`
* `./openai_crime.sh`
  * uses `prompt_crime.txt`, `categories_crime.txt`
  * modify `data_crime.txt`


To extract text / labels from result:

* `cat $OUTPUT_FILE | jq -r '.choices[0].message.content' | jq '.examples[].text'`
* `cat $OUTPUT_FILE | jq -r '.choices[0].message.content' | jq '.examples[].index'`

Submission to API:

* Build a json array in excel with `{text, label}` data

---

_Deprecated Training Setup Below__

## Training:

* Approach: few shot learning, fine-tuning via hugging face SetFit framework.
* Training data: ATX, NYC, CHI, SFO labels
* Model: [SetFit: setfit-bge-small-v1.5-sst2-8-shot](https://github.com/huggingface/setfit)


Training Notes:

* Need to use GPU
  * AWS: G4dn ~ 1hr @ 582 examples; batch 32, 7 epochs -> 15/sec
  * error dwindles fast, likely do fewer iterations
* Make sure use compatible image: OSS Nvidia image (spot ~ .20/hr)

```
# on aws instance:

pip install torch==2.2.1 transformers==4.38.2 huggingface_hub==0.21.4 \
    scikit-learn evaluate accelerate datasets setfit

python3.9 set_fit_crime.py

# for inference machine

docker exec -it <nuisancemap container> bash

python -i set_fit_load.python
```

Test set

* Boston: crime - .89
* Dallas: crime - .85
* Aggregate: crime - .887

## Files

* `Dockerfile`: environment for python classifier
* `data.ods`: excel sheet to compare test/train data
* `classifier-demo`: transformer and hugging face pipeline
* `zero_shot_311.py`: zero shot model w/ hugging face pipeline
* `set_fit_crime.py`: few shot model using hugging face setfit framework
* `set_fit_311.py`: few shot model using hugging face setfit framework
* `set_fit_load_311.py`: few shot model using hugging face setfit framework
* `set_fit_load_crime.py`: few shot model using hugging face setfit framework
* `data/`: crime or 311 data; filtered by field(s)
  * `train-crime.json`: merged training set of atx, nyc, chi, sfo labeled classes
  * `test-crime.json`: bos, dfw labeled classes
* `models/setfit-bge-small . . ./`: dir for any saved models used for inference reloading
* `examples/`: scratchpad

## Process To Label New Source

1. Find new data url, identify fields for import

* update `source_config.json`, eventually submit to `/source_config` endpoint
  (treat as data instead of config)

2. query for distinct categories:
   * `https://data.austintexas.gov/resource/fdj4-gpfu.json?$query=select
     distinct `field` > output.json`

3. `cat output.json | jq '.[].field'`

4. place output.json array in excel for labeling and paste into
   `set_fit_load.py` for prediction.

```
[
  { 'text': "ABANDONED REFRIGERATOR", 'label': 6 },
  { 'text': "ABUSE OF 911", 'label': 5 },
  ...
]
```

5. run `set_fit_load.py` to predict labels.

6. Paste into excel and manually verify category labels

7. Submit finalized text and category label to `/rawcategorylabels` endpoint
   (treat as data)

8. RawCategoryLabels:

* resource of text, label to be loaded as in memory hashmap - used for quick
  lookup in buildEntity

9. Classifier Categories

* handful of classes describing crimes/311 used to filter / group
* `classifier_categories.json`
* Keep in `api/resources` for single source of truth
  * `api `will upsert on every worker pass to ensure consistency with table
* Used by classifier
* Used by `api` to seed in `Category` table
* for `web` an endpoint retrieves from table.


```
# classifier_categories.json
# subcategories empty for now

# have to share with web - api - classifier
# once set in db - has to be sticky

{
    "crime" : [
        {
            name: violent,
            label_id: 0,
        },
        {
            name: fraud,
            label_id: 1,
            subcategories: [
              {
                  label_id: 3,
                  name: fraud-1,
              },
              {
                  label_id: 4,
                  name: fraud-2,
              }
          },
          {
              label_id: 2,
              name: burglary
          }
          ],
        }
    ],

    "311": [
        noise,
        ...
    ]
}


```
