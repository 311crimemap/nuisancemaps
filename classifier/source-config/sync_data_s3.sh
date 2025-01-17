#!/usr/bin/bash

#
# requires ~/.aws/credentials
#

source ../../.env.dev

AWS_REGION=$AWS_REGION
AWS_PROFILE=$AWS_PROFILE
AWS_SYNC_BUCKET=$AWS_SYNC_BUCKET

aws s3 sync --region $AWS_REGION --profile $AWS_PROFILE data s3://$AWS_SYNC_BUCKET
