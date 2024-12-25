#!/bin/bash

#
# because IP poisoned on worker, setting a cron job to download daily dump
# and copy over to s3
#
# see `crontab -l` for usage
#

AWS_PROFILE=311crimemap
REGION=us-east-2
URL=https://www.stlouis-mo.gov/data/upload/data-files/csb.zip
FILE=2024.csv

date
echo "[cron_data_s3] Downloading File"

mkdir -p $WORKDIR
cd $WORKDIR
wget $URL -O $WORKDIR/csb.zip

echo "[cron_data_s3] Unzipping archive"
unzip -o $WORKDIR/csb.zip $FILE

echo "[cron_data_s3] Uploading to AWS"
# Example: Use AWS CLI to upload the file to S3
aws s3 cp --profile $AWS_PROFILE --region $REGION \
    $WORKDIR/2024.csv $BUCKET
date
