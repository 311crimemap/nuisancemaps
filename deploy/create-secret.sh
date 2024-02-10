#!/usr/bin/bash

source ../.env

sudo kubectl delete secret postgresql-secret --ignore-not-found=true
sudo kubectl create secret generic postgresql-secret --from-literal=POSTGRESQL_PASSWORD=$POSTGRESQL_PASSWORD
