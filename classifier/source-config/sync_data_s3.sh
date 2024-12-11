#!/usr/bin/bash

aws s3 sync --region us-east-2 --profile 311crimemap data s3://311crimemap-data-source-config
