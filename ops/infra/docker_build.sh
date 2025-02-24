#!/usr/bin/env bash
IMAGE_REPO=$(grep -E '^(IMAGE_REPO)' .env | awk -F'=' '{print $2}')
TAG="$IMAGE_REPO/311crimemap/ops"

docker build -t $TAG .
