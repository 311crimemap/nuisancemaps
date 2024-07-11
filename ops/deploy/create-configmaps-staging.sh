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

# postgres
kubectl apply -f staging/postgresql/postgresql-configmap.yml

# pgbackrest
kubectl apply -f staging/postgresql/pgbackrest-configmap.yml

# liquibase (migrations)
kubectl delete configmap liquibase-properties-configmap --ignore-not-found=true
kubectl create configmap liquibase-properties-configmap --from-file=../../api/src/main/resources/liquibase.properties

kubectl delete configmap liquibase-changelog-master-configmap --ignore-not-found=true
kubectl create configmap liquibase-changelog-master-configmap --from-file=../../api/src/main/resources/db/changelog-master.yml

kubectl delete configmap liquibase-changelogs-configmap --ignore-not-found=true
kubectl create configmap liquibase-changelogs-configmap --from-file=../../api/src/main/resources/db/changelogs
