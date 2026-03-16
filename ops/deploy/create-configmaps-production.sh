#!/usr/bin/bash
#
# liquibase required dependencies for migration
# 1. liquibase.properties
# 2. changelog-master.yml (with filter information)
# 3. /db/changelogs/: sql migration files
#
# these are all copied into configmaps and mounted into a standalone liquibase
# container (independent of Spring) where liquibase:update is run via command line
#

# hetzner
kubectl apply -f hcloud-csi.yml

# labels from terraform node to k3s node: traefik lb
kubectl get nodes -l "node.kubernetes.io/enablelb=true" -o name | xargs -I {} kubectl label {} svccontroller.k3s.cattle.io/enablelb=true

# postgres
kubectl apply -f production/postgresql/postgresql-configmap.yml

# pgbackrest
export $(grep -E '^(PGBACKREST_REPO2_S3_(REGION|ENDPOINT|BUCKET))' ../../.env | xargs)
export $(grep -E '^(PGBACKREST_STANZA)' ../../.env | xargs)
envsubst '${PGBACKREST_REPO2_S3_REGION} ${PGBACKREST_REPO2_S3_ENDPOINT} ${PGBACKREST_REPO2_S3_BUCKET} ${PGBACKREST_STANZA}' < production/postgresql/pgbackrest-configmap.yml | kubectl apply -f -

# liquibase (migrations)
kubectl delete configmap liquibase-properties-configmap -n crimemap --ignore-not-found=true
kubectl create configmap liquibase-properties-configmap -n crimemap --from-file=../../api/src/main/resources/liquibase.properties

kubectl delete configmap liquibase-changelog-master-configmap -n crimemap --ignore-not-found=true
kubectl create configmap liquibase-changelog-master-configmap -n crimemap --from-file=../../api/src/main/resources/db/changelog-master.yml

kubectl delete configmap liquibase-changelogs-configmap -n crimemap --ignore-not-found=true
kubectl create configmap liquibase-changelogs-configmap -n crimemap --from-file=../../api/src/main/resources/db/changelogs
