#!/bin/bash

#
# because IP poisoned on worker, setting a cron job to download daily dump
# and copy over to s3
#
# see `crontab -l` for usage
#

URL=https://data.boston.gov/datastore/dump/1a0b420d-99f1-4887-9851-990b2a5a6e17?bom=True
FILE=boston_311_2026.csv

date
echo "[boston_311_2026_s3] Downloading File"

mkdir -p $WORKDIR
cd $WORKDIR
wget $URL -O $WORKDIR/$FILE


echo "[boston_311_2026_s3] Uploading to AWS"
# Example: Use AWS CLI to upload the file to S3
aws s3 cp --profile $AWS_PROFILE --region $AWS_REGION \
    $WORKDIR/$FILE $BUCKET_BOSTON
date
