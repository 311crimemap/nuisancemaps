#!/usr/bin/env bash

TAG="058264272856.dkr.ecr.us-east-2.amazonaws.com/311crimemap/ops"

docker build -t $TAG .
