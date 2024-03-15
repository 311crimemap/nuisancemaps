# Classifier

Goal of classifier is to generate category labels for quick comparison. The
expectation is to still perform a manual pass, but just make it faster.

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

pip install torch==2.2.1 transformers==4.38.2 scikit-learn evaluate accelerate datasets setfit

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
* `set_fit_crime.py`: trains model, saves and creates zip archive of model
* `set_fit_load.py`: loads saved model, model.predicts on inputs array.
* `data/`: crime or 311 data; filtered by field(s)
  * `train-crime.json`: training set of atx, nyc, chi, sfo
  * `test-crime.json`: bos, dfw
* `models/setfit-bge-small . . ./`: dir saved model contents
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



