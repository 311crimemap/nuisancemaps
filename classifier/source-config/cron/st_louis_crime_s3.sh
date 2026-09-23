#!/bin/bash
set -euo pipefail

#
# because IP poisoned on worker, setting a cron job to download daily dump
# and copy over to s3
#
# see `crontab -l` for usage
#

MONTH=$(date -d "last month" +%B)        # e.g., "April"
DATA_YEAR=$(date -d "last month" +%Y)    # e.g., "2026"
UPLOAD_YEAR=$(date +%Y)                  # current year
UPLOAD_MONTH=$(date +%m)                 # current month, zero-padded

URL="https://slmpd.org/wp-content/uploads/${UPLOAD_YEAR}/${UPLOAD_MONTH}/${MONTH}${DATA_YEAR}.csv"
#OUT="${MONTH}${DATA_YEAR}.csv"
OUT_FILE=crime.csv

date
echo "[st_louis_crime_s3] Downloading File"
mkdir -p $WORKDIR
cd $WORKDIR
wget $URL -O $WORKDIR/$OUT_FILE


echo "[st_louis_crime_s3] Uploading to AWS"
# Example: Use AWS CLI to upload the file to S3
aws s3 cp --profile $AWS_PROFILE --region $AWS_REGION \
    $WORKDIR/$OUT_FILE $BUCKET_ST_LOUIS
date
