#!/usr/bin/python

#
# python3 generate_urls.py
#
# siege  -f prod.txt -c 10 -t 300s
# siege  -f stage.txt -c 10 -t 300s
#

import json
import os

baseProductionURL= "https://api.311crimemap.com"
baseStagingURL= "https://staging-api.311crimemap.com"

#baseURL = baseStagingURL
baseURL = baseProductionURL

#FILENAME = "stage.txt"
FILENAME = "prod.txt"

COUNT = 1000
INC = .0001     #NB: param string will truncate precision > 4 digits


def write_urls(filename, urls):
    with open(filename, "w") as file:
        for url in urls:
            file.write(url +"\n")

def buildURLList(baseURL, sw_lat, sw_lng, ne_lat, ne_lng, count = 1000, step = 0.001):

    # Generate the list of URLs
    url_list = []

    for urlType in ["datacrimes.geojson?", "data311s.geojson?"]:

        for i in range(count):
            paramStr = f"sw_lat={sw_lat:.4f}&sw_lng={sw_lng:.4f}&ne_lat={ne_lat:.4f}&ne_lng={ne_lng:.4f}"
            url = "/".join([baseURL, urlType + paramStr])
            url_list.append(url)

            # Increment the values
            sw_lat += step
            sw_lng += step
            ne_lat += step
            ne_lng += step


    return url_list


#
# atx
#
sw_lat = 30.2
sw_lng = -98.0
ne_lat = 30.4
ne_lng = -97.5

atx = buildURLList(baseURL, sw_lat, sw_lng, ne_lat, ne_lng, COUNT, INC)


#
# chi
#
sw_lat = 41.8
sw_lng = -87.8
ne_lat = 42
ne_lng = -87.4

chi = buildURLList(baseURL, sw_lat, sw_lng, ne_lat, ne_lng, COUNT, INC)


#
# dfw
#
sw_lat = 32.7
sw_lng = -97
ne_lat = 32.9
ne_lng = -96.6

dfw = buildURLList(baseURL, sw_lat, sw_lng, ne_lat, ne_lng, COUNT, INC)

#
# san francisco
#
sw_lat = 41.8
sw_lng = -87.8
ne_lat = 42
ne_lng = -87.4

sfo = buildURLList(baseURL, sw_lat, sw_lng, ne_lat, ne_lng, COUNT, INC)

#
# bos
#
sw_lat = 42.3
sw_lng = -71.3
ne_lat = 42.5
ne_lng = -70.8


bos = buildURLList(baseURL, sw_lat, sw_lng, ne_lat, ne_lng, COUNT, INC)

#
# nyc
#
sw_lat = 40.6
sw_lng = -74.2
ne_lat = 40.9
ne_lng = -73.8

nyc = buildURLList(baseURL, sw_lat, sw_lng, ne_lat, ne_lng, COUNT, INC)

# write
write_urls(FILENAME, atx + chi + dfw + sfo + bos + nyc)



