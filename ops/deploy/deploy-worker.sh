#!/usr/bin/bash

kubectl apply -f base/worker/spring-worker-pv.yml
kubectl apply -f base/worker/spring-worker-pvc.yml

export $(grep -E '^(IMAGE_REPO)' ../../.env | xargs)
envsubst '${IMAGE_REPO}' < base/worker/spring-worker-deployment.yml | kubectl apply -f -
