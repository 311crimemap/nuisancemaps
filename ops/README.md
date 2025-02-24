# Ops

Ops contains info to spin up servers, create k3s cluster, and deploy containers.

## Contents

* [Benchmark](./benchmark): benchmark for testing api, db capacity
* [Infra](./infra): Packer, Terraform, Ansible infra deployment
* [Deploy](./deploy): k3s cluster deployment config
* [update_sources.sh](./update_sources.sh): update data script to submit a new round of jobs


### Infra

* `./docker_ops.sh SSH_KEY=<path to ssh key> ENV=<environemnt> ENV_ID=<env id>`:
  run commands below in docker container:

* Packer: [create base image](./infra/packer/README.md)

```
cd /infra/packer
packer init ubuntu.pkr.hcl
packer build ubuntu.pkr.hcl
```

* Terraform: create servers
  * Symlink points to "current" production configuration, but additional deploys
    are equally valid.
  * `environments/<env_id>/main.tf`: configuration leveraging modules

```
cd /infra/terraform/environments/<env>

terraform init
terraform apply
```

* Ansible: setup k3s cluster
  * NB: the env_id requires underscores for hyphens
  * need to download `~./kube/config` to bastion
  * set `KUBECONFIG` var to downloaded `~./kube/config`


```

cd /infra/ansible

# spin up
ansible-playbook -e env_id=production_blue -i hcloud.yml playbooks/site.yml

```


### K3s Deploy Sequence

k3s deploy containers order: [./deploy/README.md](README.md)

