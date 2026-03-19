#!/usr/bin/bash

source ../../.env

# Initial Namespaces
kubectl apply -f base/namespaces.yml

# Hetzner
kubectl delete secret hcloud --ignore-not-found=true --namespace=kube-system
kubectl create secret generic hcloud --namespace=kube-system --from-literal=token=$HCLOUD_TOKEN

# Cloudflare
kubectl delete secret cloudflare-api-token-secrets \
        --ignore-not-found=true --namespace=cert-manager
kubectl create secret generic cloudflare-api-token-secrets \
        --namespace=cert-manager \
        --from-literal=api-token=$CLOUDFLARE_API_TOKEN

# ECR
kubectl delete secret regcred -n crimemap --ignore-not-found=true
kubectl create secret docker-registry regcred \
        -n crimemap \
        --docker-server=$IMAGE_REPO \
        --docker-username=AWS \
        --docker-password=`aws ecr get-login-password --profile $AWS_PROFILE --region $AWS_REGION` \
        --docker-email=abc@abc.com

kubectl delete secret regcred --ignore-not-found=true
kubectl create secret docker-registry regcred \
        --docker-server=$IMAGE_REPO \
        --docker-username=AWS \
        --docker-password=`aws ecr get-login-password --profile $AWS_PROFILE --region $AWS_REGION` \
        --docker-email=abc@abc.com

# postgresql
kubectl delete secret postgresql-secrets -n core-db --ignore-not-found=true
kubectl create secret generic postgresql-secrets \
        -n core-db \
        --from-literal=POSTGRESQL_PASSWORD=$POSTGRESQL_PASSWORD \
        --from-literal=POSTGRESQL_POSTGRES_PASSWORD=$POSTGRESQL_POSTGRES_PASSWORD \
        --from-literal=password=$POSTGRESQL_PASSWORD \
        --from-literal=repmgr-password=$REPMGR_PASSWORD \
        --from-literal=admin-password=$PGPOOL_PASSWORD

kubectl delete secret postgresql-secrets -n crimemap --ignore-not-found=true
kubectl create secret generic postgresql-secrets \
        -n crimemap \
        --from-literal=POSTGRESQL_PASSWORD=$POSTGRESQL_PASSWORD \
        --from-literal=POSTGRESQL_POSTGRES_PASSWORD=$POSTGRESQL_POSTGRES_PASSWORD \
        --from-literal=password=$POSTGRESQL_PASSWORD \
        --from-literal=repmgr-password=$REPMGR_PASSWORD \
        --from-literal=admin-password=$PGPOOL_PASSWORD

# pgbackrest / postgresql
kubectl delete secret pgbackrest-secrets -n core-db --ignore-not-found=true
kubectl create secret generic pgbackrest-secrets \
        -n core-db \
        --from-literal=PGPASSWORD=$POSTGRESQL_PASSWORD \
        --from-literal=PGBACKREST_REPO2_S3_KEY_SECRET=$PGBACKREST_REPO2_S3_KEY_SECRET \
        --from-literal=PGBACKREST_REPO2_S3_KEY=$PGBACKREST_REPO2_S3_KEY


# api
kubectl delete secret api-secrets -n crimemap --ignore-not-found=true
kubectl create secret generic api-secrets \
        -n crimemap \
        --from-literal=ADMIN_API_KEY=$ADMIN_API_KEY \
        --from-literal=VITE_MAPTILER_API_KEY=$VITE_MAPTILER_API_KEY \
        --from-literal=GEOAPIFY_API_KEY=$GEOAPIFY_API_KEY

# grafana
kubectl delete secret grafana-secrets --ignore-not-found=true
kubectl create secret generic grafana-secrets \
        --from-literal=GRAFANA_PASSWORD=$GRAFANA_PASSWORD
