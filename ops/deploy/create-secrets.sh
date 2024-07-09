#!/usr/bin/bash

source ../../.env

# Hetzner
kubectl delete secret hcloud --ignore-not-found=true --namespace=kube-system
kubectl create secret generic hcloud --namespace=kube-system --from-literal=token=$HCLOUD_TOKEN

# ECR
kubectl delete secret regcred --ignore-not-found=true
kubectl create secret docker-registry regcred \
        --docker-server=058264272856.dkr.ecr.us-east-2.amazonaws.com \
        --docker-username=AWS \
        --docker-password=`aws ecr get-login-password --profile 311crimemap --region us-east-2` \
        --docker-email=abc@abc.com

# postgresql
kubectl delete secret postgresql-secrets --ignore-not-found=true
kubectl create secret generic postgresql-secrets \
        --from-literal=POSTGRESQL_PASSWORD=$POSTGRESQL_PASSWORD \
        --from-literal=POSTGRESQL_POSTGRES_PASSWORD=$POSTGRESQL_POSTGRES_PASSWORD

# pgbackrest / postgresql
kubectl delete secret pgbackrest-secrets --ignore-not-found=true
kubectl create secret generic pgbackrest-secrets \
        --from-literal=PGPASSWORD=$POSTGRESQL_POSTGRES_PASSWORD \
        --from-literal=PGBACKREST_REPO2_S3_KEY_SECRET=$PGBACKREST_REPO2_S3_KEY_SECRET \
        --from-literal=PGBACKREST_REPO2_S3_KEY=$PGBACKREST_REPO2_S3_KEY


# api
kubectl delete secret api-secrets --ignore-not-found=true
kubectl create secret generic api-secrets \
        --from-literal=ADMIN_API_KEY=$ADMIN_API_KEY

