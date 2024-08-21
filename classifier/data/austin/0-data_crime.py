#!/usr/bin/env python
#
# usage:
# python 0-data_crime.py 07/22/2024
#
import os
import json
import requests
import argparse
from bs4 import BeautifulSoup


parser = argparse.ArgumentParser(description="APD Incident Report data request: `python 0-data_crime.py 08/01/2024`")
parser.add_argument('date', type=str, help="08/01/2024")
args = parser.parse_args()

# date range: t + 6
# so if 07/01/2024; results returned are [07/01/2024, 07/07/2024] (inclusive)
# next crawl would be 07/08/2024 (increment inputs by 7 days)
STARTDATE = args.date
NUMDAYS = 6  # max accepted by server

cookies = {
    "APD_IRSEARCH": "true"
}

URL=f"https://services.austintexas.gov/police/reports/search2.cfm?startdate={STARTDATE}&numdays={NUMDAYS}&address=&rucrext=&tract_num=&zipcode=&zone=&district=&city=&choice=criteria&Submit=Submit"

startdate = STARTDATE.replace("/", "-")
filename = f"data_crime-{startdate}.html"


def save_file(html_content, filename):
    print("Saving file:", filename)
    with open(filename, "w") as file:
        file.write(html_content)

def read_file(filename):
    with open(filename, "r") as file:
        html_content = file.read()
    return html_content

def extract(td):
    offenses = []
    for j in range(0, len(td)):
        text = td[j].text.strip()
        val = {"crime": text}
        offenses.append(val)
    return offenses

#
# MAIN
#

print("Requesting: ", URL)
if os.path.exists(filename):
    print("previous file found: ", filename)
    html_content = read_file(filename)
else:
    response = requests.get(URL, cookies=cookies)
    html_content = response.text
    save_file(html_content, filename)


data = []

# Parse the HTML content with BeautifulSoup
print("Parsing . . .")

soup = BeautifulSoup(html_content, 'html.parser')
tables = soup.select('div.container > table')

# collect tables
# first table outlier
print("Tables . . .")
first_td = tables[0].select("tr table tr:nth-of-type(5) td:nth-of-type(2) td")
data += extract(first_td)


# extract
print("Extract . . .")

for i in range(1, len(tables), 2):
    if i <= len(tables):
        td = tables[i].select("tr:nth-of-type(5) td:nth-of-type(2) td")
        data += extract(td)

outFile = f"data_crime_{startdate}.json"
print(f"Writing to file: {outFile}")

with open(outFile, 'w') as file:
    json.dump(data, file)
