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

##### Certs

* `kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.15.1/cert-manager.yaml`
* wait until webhook error resolves
* `kubectl apply -f staging/cert-manager-issuer.yml`
* `kubectl apply -f base/api/spring-api-ingress.yml`
* Verify: `kubectl get cert`

##### Deployments / Service

* `kubectl apply -f base/postgresql/`
* `kubectl apply -f base/api/`
* `kubectl apply -f base/worker/`

#### Jobs

* `kubectl apply -f base/jobs/spring-db-migration-job.yml`

---


### Certs

#### 1. Get Cert Manager:

[Reference](https://cert-manager.io/docs/installation/kubectl/)

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

Let's Encrypt has production rate limit, so use staging issuer when figuring out configuration

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
