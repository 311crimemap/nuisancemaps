#!usr/bin/env python3

import requests
from urllib.parse import urlparse, parse_qs, urlencode, urlunparse
import os
import json

API_HOST="https://api.311crimemap.com"
#API_HOST="http://localhost:8080"

#
# Request Sources
#
sourcesURL = f"{API_HOST}/sources"

headers = {
    "content-type": "application/json",
    "X-API-KEY": os.getenv("ADMIN_API_KEY", "1234")
}

def add_update_cache_to_url(url_string):
    parsed_url = urlparse(url_string)
    query_params = parse_qs(parsed_url.query)

    # add updateCache
    query_params["updateCache"] = ["true"]
    new_query_string = urlencode(query_params, doseq=True)

    # Rebuild the full URL with the modified query string
    updated_url = urlunparse(parsed_url._replace(query=new_query_string))
    return updated_url


#
# initial source request
#
sourceResponse = requests.get(sourcesURL, headers=headers)
if (sourceResponse.status_code != 200):
    print(f"Error: {sourceResponse.status_code}")
    exit(1)


jsonText = json.loads(sourceResponse.text)
sources = jsonText['data']


#
# Filter for source where 'recurring=true'
#
recurringSources = [source for source in sources if source['recurring'] is True]


#
# Preprocess: any hub.arcgis.com append param: want "pre-request" request w/ updateCache = true
# before job submission
#

for source in recurringSources:
    sourcesURL = source['url']
    if "hub.arcgis.com" in sourcesURL:
        sourcesURL = add_update_cache_to_url(sourcesURL)
        res = requests.get(sourcesURL)
        print(f"{sourcesURL}: {res.status_code}")

print("--Submitting Data Jobs--")

#
# Create all recurring DataJobs to API
#
for source in recurringSources:
    sourceId = source['id']
    sourceURL = f"{API_HOST}/datajobs/sources/{sourceId}"
    print(sourceURL)
    sourceResponse = requests.post(sourceURL, headers=headers)
    print(f"{sourceResponse.status_code} | {sourceResponse.text}" )
