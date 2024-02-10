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
kubectl apply -f postgresql/postgresql-configmap.yml

# liquibase (migrations)
kubectl create configmap liquibase-properties-configmap --from-file=../api/src/main/resources/liquibase.properties
kubectl create configmap liquibase-changelog-master-configmap --from-file=../api/src/main/resources/db/changelog-master.yml
kubectl create configmap liquibase-changelogs-configmap --from-file=../api/src/main/resources/db/changelogs

