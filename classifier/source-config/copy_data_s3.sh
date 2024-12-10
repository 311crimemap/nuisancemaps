#!/usr/bin/bash

aws s3 cp --region us-east-2 --profile 311crimemap --recursive data s3://311crimemap-data-source-config
