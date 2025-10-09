#!/bin/bash

#
# because IP poisoned on worker, setting a cron job to download daily dump
# and copy over to s3
#
# see `crontab -l` for usage
#

URL=https://hfdapp.houstontx.gov/311/311-CRIS-Public-Data-Extract-D365-MTD-compressed.txt
FILE=311-CRIS-Public-Data-Extract-D365-MTD-compressed.txt

date
echo "[houston_311_MTD_s3] Downloading File"

mkdir -p $WORKDIR
cd $WORKDIR
wget $URL -O $WORKDIR/$FILE


echo "[houston_311_MTD_s3] Uploading to AWS"
# Example: Use AWS CLI to upload the file to S3
aws s3 cp --profile $AWS_PROFILE --region $AWS_REGION \
    $WORKDIR/$FILE $BUCKET_HOUSTON
date
