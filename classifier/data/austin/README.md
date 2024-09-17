# Notes

## Crime 0-Data

Extract single column from csv:

1. `csvcut -c "Highest Offense Description" crimereport_20240708.csv > crimereport_20240708.txt`
2. `tail -n +2 crimereport_20240708.txt | sort | uniq > crimereport_20240708_sorted.txt`

1. `cat data_crime_*.json > data_crime_data.json`
2. `cat data_crime_dated.json | jq -r '.[].crime' > data_crime_dated.txt`
3. `sort data_crime_dated.txt | uniq > data_crime_dated_sorted.txt`


1. `cat crimereport_20240708_sorted.txt data_crime_dated_sorted.txt > data_crime_all.txt`
2. `sort data_crime_all | uniq > data_crime.txt`


## 311 0-Data

notice use group by:

1. `curl <datasource>?$select=<column>&$group=<column>&$limit=100000 > data_311.json`
2. `cat data_311.json | jq -r '[].sr_type_desc' > data_311.txt`


