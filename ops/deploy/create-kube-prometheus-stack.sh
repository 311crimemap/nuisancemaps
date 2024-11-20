#!/usr/bin/bash
#
#
helm install prometheus prometheus-community/kube-prometheus-stack \
     --set grafana.adminPassword="$(kubectl get secret grafana-secrets -o jsonpath="{.data.GRAFANA_PASSWORD}" | base64 --decode)" \
     -f base/kube-prometheus-stack/values.yml
