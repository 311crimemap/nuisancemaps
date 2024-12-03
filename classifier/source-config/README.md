# Source Config
---

## Source urls pattern from id key

* CSV: https://data.buffalony.gov/api/views/d6g9-xbgu/rows.csv?accessType=DOWNLOAD
* JSON: https://data.buffalony.gov/resource/d6g9-xbgu.json?$limit=2


## Template -> Source Config

### Prompt

`./prompt/.1-gen-source-config.txt`:

#### Obtaining User Data

Snippet of JSON or CSV data from url source e.g $?limit=3 (Example commands)

* JSON: `curl -s 'https://data.buffalony.gov/resource/d6g9-xbgu.json?$limit=3' > buffalo.json`
* CSV: `curl -s 'https://data.buffalony.gov/api/views/d6g9-xbgu/rows.csv?accessType=DOWNLOAD' | awk 'NR > 3 { exit } { print }' > buffalo.csv`

NB: use awk here since `curl | head -n 3` doesn't (OS Specific) always send
SIGPIPE to indicate end of process, so curl could continue to download in
background. Does seem to work in ubuntu, but using awk for robustness.

Submit ~3 examples as user role.

---

## Source Config -> Method Gen

* `./2-generate-source-methods.py <source_config.out.json> <sample_data.json>`
* e.g.: `./2-generate-source-methods.py buffalo.json.out.json buffalo.json`

### Prompt

`./prompt/.2=generate-source-methods.txt`:

USER INPUT:

1. source config json from previous step (`1-generate-source-config.py`)
2. sample data Same as above snippet of json or csv data

---

## Verify; add any method gen, use in Source Config.

* Determine if `*.json.methods.txt` has separate methods
* add methods to api `service/parserstrategy/*.java` city methods page.
* add source_config to source-config.json
* submit and verify
