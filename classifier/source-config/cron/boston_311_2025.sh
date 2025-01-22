#!/bin/bash

#
# because IP poisoned on worker, setting a cron job to download daily dump
# and copy over to s3
#
# see `crontab -l` for usage
#

URL=https://data.boston.gov/datastore/dump/9d7c2214-4709-478a-a2e8-fb2020a5bb94?bom=True
FILE=boston_311_2025.csv

date
echo "[boston_311_2025_s3] Downloading File"

mkdir -p $WORKDIR
cd $WORKDIR
wget $URL -O $WORKDIR/$FILE


echo "[boston_311_2025_s3] Uploading to AWS"
# Example: Use AWS CLI to upload the file to S3
aws s3 cp --profile $AWS_PROFILE --region $AWS_REGION \
    $WORKDIR/$FILE $BUCKET_BOSTON
date
