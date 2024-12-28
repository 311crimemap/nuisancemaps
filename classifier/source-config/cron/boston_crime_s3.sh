#!/bin/bash

#
# because IP poisoned on worker, setting a cron job to download daily dump
# and copy over to s3
#
# see `crontab -l` for usage
#

AWS_PROFILE=311crimemap
REGION=us-east-2
URL=https://data.boston.gov/datastore/dump/b973d8cb-eeb2-4e7e-99da-c92938efc9c0?bom=True
FILE=boston_crime.csv


date
echo "[boston_crime_s3] Downloading File"

mkdir -p $WORKDIR
cd $WORKDIR
wget $URL -O $WORKDIR/$FILE


echo "[boston_crime_s3] Uploading to AWS"
# Example: Use AWS CLI to upload the file to S3
aws s3 cp --profile $AWS_PROFILE --region $REGION \
    $WORKDIR/$FILE $BUCKET_BOSTON
date
