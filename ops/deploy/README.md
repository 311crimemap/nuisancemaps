# K3s Deploy

Deploy Notes.

## Order

Try to keep production and staging environments similar; except for deviation with some
configmap files (pgbackrest)

##### Secrets

`./create-secrets.sh`:

* `hcloud-secret`: hcloud-secret.yml (add TOKEN)
* `regcred`: see description below
* `postgresql / pgbackrest`: env contents
* `api`: env contents

##### ConfigMap

`./create-configmaps-<environment>.sh`:

* `hcloud-csi.yml`: hetzner classes
* `-postgresql-configmap`: postgres
* `pgbackrest-configmap`: pgbackrest
  * Make sure s3 bucket is created prior to backup (pgbackrest does not do this)
* `liquibase`: for migration; db schema

##### Traefik

* `kubectl apply -f base/traefik`: add k3s nodeSelector and tolerances for traefik placement; enable gzip compression

##### Certs

* `kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.15.1/cert-manager.yaml`
  * wait until webhook error resolves (~ 1min)
  * Verify: `kubectl get pods -A`
* `kubectl apply -f production/cert-manager-issuer.yml`  # PRODUCTION
* `kubectl apply -f staging/cert-manager-issuer.yml` # STAGING
  * Verify: `kubectl describe clusterissuer`
* `kubectl apply -f <env>/api/spring-api-ingress.yml`
  * Verify: `kubectl get cert`  # 30 sec; should read "READY True"
    * Intermediate steps:
      * `kubectl describe orders`
      * `kubectl describe challenges`

- clean up / redo:
* kubectl get secrets / get cert -> delete these
* kubectl delete -f <env>/api/spring-api-ingress.yml
* kubectl delete -f <env>/cert-manager-issuer.yml

##### PV Reclaim Policy

In case pv's ReclaimPolicy is to Delete:

```
kubectl patch pv <pv-name> -p '{"spec":{"persistentVolumeReclaimPolicy":"Retain"}}'
```


##### Monitoring

Note: there isn't an equivalent "useExistingSecret" in helm config, so using command line.

Chart values.yml: https://raw.githubusercontent.com/prometheus-community/helm-charts/refs/heads/main/charts/kube-prometheus-stack/values.yaml

Not consistent component labels, need to check each component for proper label hierarchy nodeSelector.

```
# install prometheus & grafana
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# /monitor
# calls helm, assigns password, sets labels to assign pods to control node.
#
./create-kube-prometheus-stack.sh

```

##### Deployments / Service

* `kubectl apply -f base/postgresql/`
* `helm install crimemap-db bitnami/postgresql-ha --version 14.3.1 -f base/postgresql/values.yml`
* `kubectl apply -f base/api/`
* `kubectl apply -f base/worker/`


#### Logical Restore

`gunzip -c dump.sql.gz | psql -U postgres -d nuisancemaps`


#### Scale up/down DB

1. Ensure `postgresql-0` is primary; (or delete postgresql-1 pod to ping pong primary back to 0)
2. adjust `base/postgresql/pgpool-configmap.yml` to comment/uncomment backend_1 replica host and apply.
3. `kubectl get statefulset`
   * `kubectl scale statefulset crimemap-db-postgresql-ha-postgresql --replicas=1`
4. Update `values.yml` `postgresql.replicaCount` to match values.

AVOID any `helm upgrade` commands. This will restart each statefulset pod with
rollingUpdate; this triggers primary to replica failover.
* `DO NOT: helm upgrade crimemap-db bitnami/postgresql-ha -f values.yml`
* `DO NOT: helm upgrade crimemap-db bitnami/postgresql-ha --set replicaCount=2 --reuse-values`

Depending on infra changes, may also need to delete `pvc` / `pv` if spinning down.


#### Jobs

* `kubectl apply -f base/jobs/spring-db-migration-job.yml`


#### Boostrap Data

1. Build Locales: `curl -X POST -H 'X-API-KEY: <KEY>' -H 'content-type:application/json' -d @locale_config.json http://<API_HOST>/locales/batch`

2. Build Categories: `curl -X POST -H 'X-API-KEY: <KEY>' -H 'content-type:application/json' -d @classifier_categories.json http://<API_HOST>/categories`

3. Submit TextCategories: `curl -X POST -H 'content-type:application/json' -H 'X-API-KEY: <KEY>' -d @labeled_crime.json http://<API_HOST>/textcategories`
   * NB2: if bootstrapping, will have to restart once categories submitted so
     textCategory map can build. TODO: fix this.

4. NB: Once a source is submitted, worker will try to fetch

5. Submit source (requires associated `locale` id)
  * `cat source_config.json | jq '.[11]' > test.json`
  * `curl -X POST -H 'X-API-KEY: <KEY> -H 'content-type:application/json' -d @test.json http://<API_HOST>/locales/4/sources`



---
## Spin Down

```

kubectl delete -f <manifest>

helm uninstall <name / e.g. crimemap-db>

#
# clean up and disables from further scheduling
#
kubectl drain <node-name> --ignore-daemonsets --delete-local-data


kubectl delete node <node-name>

#
# ansible
#
ansible-playbook -e env_id=staging_live -i hcloud.yml playbooks/uninstall-workers.yml

```

## Uninstall

Typically do reverse of spinning up: `kubectl delete -f <thing>`

#### Ingress / Deployments / Services
  * api
  * postgresql

#### Certs
  * cert-issuer
  * cert-manager

#### ConfigMap
#### Secrets

---


### Certs

#### 1. Get Cert Manager:

[Reference:
https://cert-manager.io/docs/installation/kubectl/](https://cert-manager.io/docs/installation/kubectl/)

`kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.15.1/cert-manager.yaml`

Installs to `cert-manager` namespace.

```
cert-manager   cert-manager-cainjector-7666685ff5-84dgd   1/1     Running     0          21s
cert-manager   cert-manager-5798486f6b-5qdmf              1/1     Running     0          20s
cert-manager   cert-manager-webhook-5f594df789-tcqfl      1/1     Running     0          20s
```


Make sure `cert-manager-webhook` is ready, or subsequent ClusterIssuer manifest will fail (Internal error occurred: failed calling webhook "webhook.cert-manager.io": failed to call webhook...). Just wait.


Uninstall: `kubectl delete -f https://github.com/cert-manager/cert-manager/releases/download/v1.15.1/cert-manager.yaml`
* error possibilities:
  * terminating namespace: `kubectl delete apiservice v1beta1.webhook.cert-manager.io`
  * pending challenge




#### 2. Configure ClusterIssuer

[Reference:
https://cert-manager.io/docs/tutorials/acme/nginx-ingress/#step-5---deploy-cert-manager](https://cert-manager.io/docs/tutorials/acme/nginx-ingress/#step-5---deploy-cert-manager)

Let's Encrypt has production rate limit, so use staging issuer when figuring out
configuration

```
# <env> / cert-manager-issuer.yml

kind: ClusterIssuer
...
    solvers:
        - http01:
            ingress:
              class: traefik
```

Apply:

```
kubectl apply -f staging/cert-manager-issuer.yml

kubectl get clusterissuers

NAME                  READY   AGE
letsencrypt-staging   True    10s


kubectl describe clusterissuer letsencrypt-staging

```

#### 3. Attach to ingress

Attaching sets endpoints for Let's Encrypt to access, verify ownership and
eventually issue cert.

##### Configuration

* add `metadata.annotations` block:

```
metadata
  annotations:
    kubernetes.io/ingress.class: "traefik"
    cert-manager.io/cluster-issuer: letsencrypt-staging
```

* `spec` block:

```
spec
  ingressClassName: traefik
  tls:
  - hosts:
    - 311crimemap.com
    # name of secret that contains the TLS certificate and private key for the hostname
    secretName: 311crimemap-com-tls

```

##### Application

```
kubectl apply -f base/api/spring-api-ingress.yml

# this can take some time

kubectl get certificate

# to figure out errors

kubectl describe challenge
kubectl describe order

```

The Staging cert will have a security warning, but you will see the organization
as `(STAGING) Let's Encrypt.`

Lifetime of cert is tied to secret, so don't need the management containers
always running.

##### Reissuing Cert

* Valid cert eventually updates secret, (specified in `cert-manager-issuer.yml`)
* To reissue delete the secret: `kubectl delete secret 311crimemap-com-tls`
* restart cert process (spin up cert manager and cert issuer containers)


---

### K3s Traefik Ingress

* Traefik is default ingress provider `-n=kube-system`, initial job installs
  traefik via helm chart.

`svclb-traefik-**`: Traefik's DaemonSet ServiceLB load balancer pods that receive external
traffic.
  * By default these are run on all nodes.
  * When label nodes with `enablelb=true`, this sets `svclb-traefik` pods to run
    only on nodes explicitly labeled with the label.

`kubectl get service/traefik -o wide -n kube-system`: show what nodes
loadbalancer is running on.

`kubectl label nodes NODENAME svccontroller.k3s.cattle.io/enablelb=true`:
enables run ServiceLB (svclb) only on labeled enabled nodes.
* There are 2/2 pods for svclb, to handle port 80, and port 443.
  (Not "duplicate" pods).

Typically want to exclude database machine or others from also taking in
traffic.

`traefik`: pod that applies routing logic. Separate from load balancer pods.

Default tolerations to avoid control-plane / master node.

#### Modifying default helm chart

* default found in k3s install: `/var/lib/rancher/k3s/server/manifests/traefik.yaml`.
* Modify via instructions: https://docs.k3s.io/helm#customizing-packaged-components-with-helmchartconfig
  * typically place as an add on manifest in /var/lib/rancher....
  * also kubectl apply -f the manifest. Just need to make sure namespace is set to `kube-system`.

```
apiVersion: helm.cattle.io/v1
kind: HelmChartConfig
metadata:
  name: traefik
  namespace: kube-system
spec:
  valuesContent: |-
    nodeSelector:
      node.kubernetes.io/class_id: app
    tolerations:
      - key: "node.kubernetes.io/class_id"
        operator: "Equal"
        value: "db"
        effect: "NoSchedule"

```

Note: "The nodeselector / tolerations control where traefik rules pod is deployed. The
ServiceLB (svclb) is controlled by the labeling nodes:
`svccontroller.k3s.cattle.io/enablelb=true`.

Not sure but it might be better to just leave svclb on all nodes, but don't
point traffic to the db node.


#### ingress-nginx configuration (experiment branch - mnanifests only written for staging environment)

Experiment to use `ingress-nginx` as ingress controller via helm. See files in
branch `experiments/ops/ingress`.

Note there are different editions of nginxl community - free, enterprise - pay,
with missing support for annotation configuration or server-snippets that are
not available.

`ingress-nginx` is the k8s version; configuration options:
https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/

##### Install

1. Install k3s disabling traefik - configuration option via ansible task
   `roles/k3s_server/tasks/main.yml` environmental variable:

`INSTALL_K3S_EXEC: "server --dsiable traefik"`

2. Install ingress-nginx

https://cert-manager.io/docs/tutorials/acme/nginx-ingress/#step-2---deploy-the-nginx-ingress-controller

* `helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx`
* `helm repo update`
* `helm install quickstart ingress-nginx/ingress-nginx` (quickstart name)

3. Add ingress resource

* `deploy/staging/api/spring-api-ingress-nginx.yml`
    * `spring-api-ingress` rules point to existing `spring-api-service` (swap
      out traefik ingress)

3a. Add additional configuration via helm

* `deploy/staging/api/spring-api-ingress-nginx-helm-config.yml`
  * addition of `use-gzip` values
  * config usage:
    * `helm install quickstart ingress-nginx/ingress-nginx -f spring-api-ingress-nginx-helm-config.yml`
    * `helm upgrade quickstart ingress-nginx/ingress-nginx -f spring-api-ingress-nginx-helm-config.yml`

4. Cert-Manager with nginx

* `deploy/staging/cert-manager-issuer-nginx.yml`
  * replace any traefik with ingress class / ingressClassName: nginx

##### Performance

`ingress-nginx` is substantially slower than traefik. Using siege (`siege -f
stage.txt -c 20 -t 300s`), number of transactions were ~5000, while traefik
would deliver ~7000 in the same period.

This is surprising and unexpected. I thought nginx would far surpass traefik.
But it might be since k3s is bundled with traefik, it has some predisposed
configuration to support traefik, or helm chart configuration for nginx is not
optimal. Other thoughts include nginx (worker) model vs traefik (event) model
that would favor traefik when dealing with concurrency.

But I just don't have time to keep digging into this.


#### haproxy-ingress configuration (experiment branch)

https://www.haproxy.com/blog/enable-tls-with-lets-encrypt-and-the-haproxy-kubernetes-ingress-controller

1. Install k3s disabling traefik (see above)

2. Install Helm Haproxy charts

```
helm repo add haproxytech https://haproxytech.github.io/helm-charts
helm repo update
```

3. Install Haproxy via Helm as DaemonSet

```
helm install haproxy haproxytech/kubernetes-ingress \
  --set controller.kind=DaemonSet \
  --set controller.daemonset.useHostPort=true \
  -f spring-api-ingress-haproxy-helm-config.yml
```

3a. Any helm configuration update - no config used in this demo.

```
helm upgrade --install haproxy haproxytech/kubernetes-ingress -f spring-api-ingress-haproxy-helm-config.yml
```

4. Run ingress

`kubectl apply -f deploy/staging/api/spring-api-ingress-haproxy.yml`
`

##### Performance

haproxy with gzip enabled and no cloudflare is significantly worse than nginx.
```
siege -f stage.txt -c 20 -t 300s
{	"transactions":			        3529,
	"availability":			      100.00,
	"elapsed_time":			      299.62,
	"data_transferred":		     2624.40,
	"response_time":		        1.48,
	"transaction_rate":		       11.78,
	"throughput":			        8.76,
	"concurrency":			       17.48,
	"successful_transactions":	        3529,
	"failed_transactions":		           0,
	"longest_transaction":		      148.08,
	"shortest_transaction":		        0.32
}
```

However with no gzip and put behind cloudflare, it's almost equivalent to
traefik. But some dropped connections I think simply because of data being sent
over.

```
siege -f stage.txt -c 20 -t 300s


{	"transactions":			        6940,
	"availability":			       99.97,
	"elapsed_time":			      299.64,
	"data_transferred":		     2175.60,
	"response_time":		        0.84,
	"transaction_rate":		       23.16,
	"throughput":			        7.26,
	"concurrency":			       19.39,
	"successful_transactions":	        6940,
	"failed_transactions":		           2,
	"longest_transaction":		        8.19,
	"shortest_transaction":		        0.25
}

```

On smaller instances seems like traefik, or the pre-build configuration is
ideal.


#### Monitoring

Using prometheus + grafana.

* Prometheus is the exporter / collector. Create service monitor resource to
  enable auto-discover. (Sometimes created in Helm Chart.)

* Typically requires labeling with the helm install name (helm install
  prometheus prometheus-community/kube-prometheus-stack): e.g. `labels.release:
  prometheus` to pick up.

* Does take some time (30s, 1m) to ingest exports and render a dashboard. Be
  patient.

##### Grafana Dashboards

* Postgres: https://grafana.com/grafana/dashboards/9628-postgresql-database/
* Spring: https://grafana.com/grafana/dashboards/14430-spring-boot-statistics-endpoint-metrics/
* Search: https://grafana.com/grafana/dashboards/


##### Prometheus

To expose service, need to link a Service Monitor resource to Service:

1. Service resource: make sure it's labeled
   * `metadata.labels`: key: value.
   * also make sure to add `name` to `ports` config

2. Service Monitor:
   * add `metadata.labels`: `release: prometheus` <- label indicates pick up by prometheus
   * add `spec.selector.matchLabels`: match Service label above key: value
   * set `endpoints.port`: match the `ports.name` in Service

```
#
# service
#

apiVersion: v1
kind: Service
metadata:
  name: spring-api-service
  labels:
    app: spring-api-service
spec:
  selector:
    app: spring-api
  ports:
    - name: "http"
      protocol: TCP
      port: 8080
      targetPort: 8080
  type: ClusterIP


#
# service monitor
#

apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: spring-api-servicemonitor
  labels:
    release: prometheus
spec:
  selector:
    matchLabels:
      app: spring-api-service  # refers to service
  endpoints:
    - port: "http"
      path: /actuator/prometheus
      interval: 15s

```

Debug Prometheus, make sure service monitor is being polled, visit the
prometheus console.

Enable console via spring-api-ingress: (make sure to also enable tls)

```
#
# spring-api-ingress.yml
#
- host: staging-prometheus.311crimemap.com
  http:
    paths:
    - path: /
      pathType: Prefix
      backend:
        service:
          name: prometheus-kube-prometheus-prometheus
          port:
            number: 9090
```

In Prometheus console; hit "target" navbar and verify monitor is populated and
exporting.

Sometimes takes 30s - 1 min to populate


---

### DB Backup / Export

To grab prod and use in dev, pipe output from prod via `pg_dump`:
NB: do not use interactive terminal `kubectl -it` as it will corrupt the output

See `nuisancemaps/db/README.md` for specifics with `postgresql-ha`, parallelism
and directory dump/restore.

```
kubectl exec postgresql-0 -- pg_dump -U postgres -Fc nuisancemaps | cat > nuisancemaps_prod.dump
```

Restore dev - ensure file is available (`/temp` mount) in `docker-compose.yml`,
or `/mnt/tmp` in prod.

##### pg_restore

Additional options:

* `-j`: jobs increases parallelism
* `-c`: clean; will drop objects. Still recommended to drop and (re)create database prior.
* `-d`: database name
* lastly, dump filename

```
pg_restore -U <dev_db_user> -j 4 -c -d nuisancemaps nuisancemaps_prod.dump
```



### DB Recovery

* location on host-0 and in container: `/backup_db/pgbackrest`

1. Create Stanza

Initially, need to create stanza (should be done already, if a backup has been
made from prod.) But if restoring from prod to staging, etc. will need to create
stanza.

Note this needs to be exec'd in a postgres container actively running pg. (e.g.
not just bash in container)

```
kubectl exec -it postgres-0 -- bash

pgbackrest --stanza=311crimemap stanza-create
pgbackrest check
```


2. Run restore job

Need to shutdown pg statefulset, and run `pgbackrest-db-restore` job which
mounts the volume and executes restore.

`kubectl apply -f jobs/pgbackrest-db-restore-job.yml`

For initial restore to different environment, may need to specify the latest
backup set, and modify the job command:

```
pgbackrest --stanza=311crimemap --repo=2 --delta \
    --set=20240702-211456F_20240702-212513D \
    --log-level-console=detail restore

```

3. Run postgres in recovery mode


`kubectl apply jobs/postgres-db-recovery-job.yml`

Might have to stanza-upgrade if recovery is on different machines.

```
kubectl get pods
kubectl kubectl exec -it postgres-db-recovery-xxxx bash

# in container
pgbackrest --stanza=311crimemap stanza-upgrade

```

Terminate postgres recovery container

`kubectl delete -f jobs/postgres-db-recovery-job.yml`

4. Restart postgres as normal service

`kubectl apply -f /postgresql`


5. Run pgbackrest backup to build local machine copy alongside s3

```
kubectl exec -it postgresql-0 -- bash
PGPASSWORD=xxxx pgbackrest --stanza=311crimemap --repo=1 --log-level-console=detail --type=full backup
```


#### Bitnami Postgresql StatefulSet

NB: make sure to delete pvc for fresh start

* Create extension requires superuser postgres
* Don't want to grant superuser privileges to database user, nor connect as superuser
* Bitnami container can run init scripts, but needs to be run as superuser
  * `POSTGRESQL_INITSCRIPTS_USERNAME`: postgres
  * `POSTGRESQL_INITSCRIPTS_PASSWORD`: as secret via .env
  * `POSTGRESQL_USER`: db user
  * `POSTGRESQL_PASSWORD`: as secret via .env
  * init script is in `postgresql/postgresql-configmap.yml`.
* init scripts needed to create postgis extension
* DB user for migration and app connections

For dev environment, currently using only superuser account, so separate init
superuser not necessary.

#### Expanding StatefulSet Volume

* Shutdown postgresql service
* On Hetzner console, wait for detach, expand filesystem
  * `postgresql-statefulset.yml`: expand `storage` key to desired amount.
* Restart postgresql service

0. ssh into machine (not container)

1. `lsblk`: check if partition is not full - might need to expand partition
  (likely no need, since volume is standalone.) Here expanded 16gb of space is
  visible, not partitioned

```
NAME    MAJ:MIN RM  SIZE RO TYPE MOUNTPOINTS
sda       8:0    0 38.1G  0 disk
├─sda1    8:1    0 37.9G  0 part /var/lib/kubelet/pods/b09d6f3f-a31f-44e3-a5f6-e08ecfc9fc9f/volume-subpaths/pgbackrest-archive-conf/postgresql/4
│                                /var/lib/kubelet/pods/b09d6f3f-a31f-44e3-a5f6-e08ecfc9fc9f/volume-subpaths/pgbackrest-pgbackrest-conf/postgresql/3
│                                /var/lib/kubelet/pods/b09d6f3f-a31f-44e3-a5f6-e08ecfc9fc9f/volume-subpaths/initdb/postgresql/2
│                                /
├─sda14   8:14   0    1M  0 part
└─sda15   8:15   0  256M  0 part /boot/efi
sdb       8:16   0   16G  0 disk /var/lib/kubelet/pods/b09d6f3f-a31f-44e3-a5f6-e08ecfc9fc9f/volumes/kubernetes.io~csi/pvc-abfa5b87-8120-4d70-b5b2-fc75d40bc8e5/mount

```

2. `resize2fs`: expand ext4 filesystem

* `df -HT`: shows a 10gb file system (even though our block is a newly expanded 16GB)

```
/dev/disk/by-id/scsi-0HC_Volume_101141679 ext4     9.8G   71M  9.7G   1% /var/lib/kubelet/pods/b09d6f3f-a31f-44e3-a5f6-e08ecfc9fc9f/volumes/kubernetes.io~csi/pvc-abfa5b87-8120-4d70-b5b2-fc75d40bc8e5/mount
```

* `sudo resize2fs /dev/disk/by-id/scsi-0HC_Volume_101141679`: expands the filesystem


---

### K3S Control Plane

```
# 0. Install

curl -sfL https://get.k3s.io | sh -

# 1. copy IP addresss for host

# 2. copy over kube config (remote: `/etc/rancher/k3s/k3s.yaml`)

scp root@<host-ip>:/etc/rancher/k3s/k3s.yaml ~/.kube/config

# 3. Change cluster.server ip from 127.0.0.1 -> <node-0-ip>

nano ~/.kube/config  # change ip

# 4. add KUBECONFIG env

export KUBECONFIG=~/.kube/config

```


Add docker ECR secret (named `regcred` in this example):

```
kubectl create secret docker-registry regcred \
    --docker-server=058264272856.dkr.ecr.us-east-2.amazonaws.com \
    --docker-username=AWS \
    --docker-password=`aws ecr get-login-password --profile 311crimemap --region us-east-2` \
    --docker-email=abc@abc.com
```

docker image push
```
# 1. Reauth if necessary

aws ecr get-login-password --region us-east-2 --profile 311crimemap | \
docker login --username AWS --password-stdin 058264272856.dkr.ecr.us-east-2.amazonaws.com

# 2. Push

docker push 058264272856.dkr.ecr.us-east-2.amazonaws.com/311crimemap/api:0.0.1-SNAPSHOT

```

---


```
sudo service k3s restart
kubectl apply -f postgres-statefulset-deployment.yml
kubectl apply -f postgres-service.yml
kubectl apply -f spring-api-deployment.yml
kubectl get pods
kubectl exec -it postgresql-0 -- bash

kubectl get all
```

#### Docker Import Image Local

To import local image into a local cluster:

1. need to create tar file first
2. import

```
docker save 311crimemap/api:0.0.1-SNAPSHOT > api-0.0.1-SNAPSHOT.tar
sudo k3s ctr images import api-0.0.1-SNAPSHOT.tar
```

#### Migration

Run standalone CLI liquibase migrations (own pod.)

```
./create-configmaps.sh

kubectl apply -f spring-db-mgration-job.yml
```


---

## Scratchpad

##### Scratch Liquibase via container

```
docker run -it \
       --network nuisancemaps_default \
       -v /home/vergeman/dev/nuisancemaps/api/src/main/resources/db/changelogs:/liquibase/db/changelogs \
       -v /home/vergeman/dev/nuisancemaps/api/src/main/resources/liquibase.properties:/liquibase/db/liquibase.properties \
       -v /home/vergeman/dev/nuisancemaps/api/src/main/resources/db/changelog-master.yml:/liquibase/db/changelog-master.yml \
       liquibase/liquibase:4.25 update \
       --defaults-file=/liquibase/db/liquibase.properties
```
