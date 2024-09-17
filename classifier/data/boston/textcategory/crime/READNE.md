# Crime

https://data.boston.gov/dataset/crime-incident-reports-august-2015-to-date-source-new-system/resource/b973d8cb-eeb2-4e7e-99da-c92938efc9c0

Query:

```
curl 'https://data.boston.gov/datastore/dump/b973d8cb-eeb2-4e7e-99da-c92938efc9c0?bom=True' > b973d8cb-eeb2-4e7e-99da-c92938efc9c0.csv

csvcut -c "OFFENSE_DESCRIPTION" | sort | uniq  > b973d8cb-eeb2-4e7e-99da-c92938efc9c0.txt
```

compare this output to `pre-2023.txt`, sort / uniq, diff.

Label, and add the new elements to `labeled-crime.csv`

Most likely just entries with quotes and internal commas.

# 311

```
curl "https://data.boston.gov/datastore/dump/dff4d804-5031-443a-8409-8344efd0e5c8?bom=True" > dff4d804-5031-443a-8409-8344efd0e5c8.csv

csvcut -c "type" | sort | uniq > new.txt

diff new.txt pre_2024.txt
```

should see there are no new text categories (all diffs are `>` point to existence only in pre_2024.txt)
