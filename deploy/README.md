# K3s Deploy

Deploy Notes.

## Order

##### Secrets

`./create-secrets.sh`:

* `hcloud-secret`: hcloud-secret.yml (add TOKEN)
* `regcred`: see description below
* `postgresql`: ./create-secret.sh

##### ConfigMap

`./create-configmaps.sh`:

* `hcloud-csi.yml`: hetzner classes
* `postgresql-configmap`: /postgres
* `create-liquibase-configmap.sh`: for migration loads configuration files

##### Deployments / Service

* `kubectl apply -f /postgresql`
* `kubectl apply -f /api`
* `kubectl apply -f /worker`


---

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
* init scripts create postgis extension
* DB user for migration and app connections


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
    --docker-server=976034468541.dkr.ecr.us-east-2.amazonaws.com \
    --docker-username=AWS \
    --docker-password=`aws ecr get-login-password --profile abrepo --region us-east-2` \
    --docker-email=abc@abc.com
```

docker image push
```
# 1. Reauth if necessary

aws ecr get-login-password --region us-east-2 --profile abrepo | \
docker login --username AWS --password-stdin 976034468541.dkr.ecr.us-east-2.amazonaws.com

# 2. Push

docker push 976034468541.dkr.ecr.us-east-2.amazonaws.com/vergeman/nuisancemaps:0.0.1-SNAPSHOT

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
docker save nuisancemaps:0.0.1-SNAPSHOT > nuisancemaps-0.0.1-SNAPSHOT.tar
sudo k3s ctr images import nuisancemaps-0.0.1-SNAPSHOT.tar
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
