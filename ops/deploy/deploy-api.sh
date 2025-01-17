#!/usr/bin/bash

export $(grep -E '^(IMAGE_REPO)' ../../.env)
envsubst '${IMAGE_REPO}' < base/api/spring-api-deployment.yml | kubectl apply -f -

kubectl apply -f base/api/spring-api-service.yml
kubectl apply -f base/api/spring-api-servicemonitor.yml
