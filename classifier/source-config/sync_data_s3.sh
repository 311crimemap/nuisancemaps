#!/usr/bin/bash

#
# requires ~/.aws/credentials
#

source ../../.env.dev

AWS_SYNC_REGION=$AWS_SYNC_REGION
AWS_SYNC_PROFILE=$AWS_SYNC_PROFILE
AWS_SYNC_BUCKET=$AWS_SYNC_BUCKET

aws s3 sync --region $AWS_SYNC_REGION --profile $AWS_SYNC_PROFILE data s3://$AWS_SYNC_BUCKET
