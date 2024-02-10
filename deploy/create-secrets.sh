#!/usr/bin/bash

source ../.env

# Hetzner
kubectl delete secret hcloud --ignore-not-found=true --namespace=kube-system
kubectl create secret generic hcloud --namespace=kube-system --from-literal=token=$HCLOUD_TOKEN

# ECR
kubectl delete secret regcred --ignore-not-found=true
kubectl create secret docker-registry regcred \
        --docker-server=976034468541.dkr.ecr.us-east-2.amazonaws.com \
        --docker-username=AWS \
        --docker-password=`aws ecr get-login-password --profile abrepo --region us-east-2` \
        --docker-email=abc@abc.com

# postgresql
kubectl delete secret postgresql-secret --ignore-not-found=true
kubectl create secret generic postgresql-secret --from-literal=POSTGRESQL_PASSWORD=$POSTGRESQL_PASSWORD
