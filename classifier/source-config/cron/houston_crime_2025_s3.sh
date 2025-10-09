#!/bin/bash

#
# because IP poisoned on worker, setting a cron job to download daily dump
# and copy over to s3
#
# see `crontab -l` for usage
#

URL=https://www.houstontx.gov/police/cs/xls/NIBRSPublicView2025.xlsx
FILE=NIBRSPublicView2025.xlsx

date
echo "[houston_crime_2025_s3] Downloading File"

mkdir -p $WORKDIR
cd $WORKDIR
wget $URL -O $WORKDIR/$FILE


echo "[houston_crime_2025_s3] Uploading to AWS"
# Example: Use AWS CLI to upload the file to S3
aws s3 cp --profile $AWS_PROFILE --region $AWS_REGION \
    $WORKDIR/$FILE $BUCKET_HOUSTON
date
