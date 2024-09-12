# Hetzner Ops

* [Packer](./packer) for initial image(s)
* [Terraform](./terraform) for infra
* [Ansible](./ansible) to setup k3s nodes, join as cluster
* K3s on local machine (with config file) to deploy pods.

On Hetzner, Access Token is generated per project, and determines which project
infra resides.


## k3s spin worker up/download


```

#
# k3s
#

kubectl drain <node-name> --ignore-daemonsets --delete-local-data

kubectl delete node <node-name>



#
# remove k3s via ansible
#

ansible-playbook -e env_id=staging_live -i hcloud.yml playbooks/uninstall-workers.yml



#
# terraform change server_count
#

module "worker app-server" {
    ...
    server_count = 0
    ...
}

terraform apply

```

### Quirks:

* The HashiCorp APT server has packages only for the amd64 architecture, so
  avoid arm/gravitron on AWS (m*g) instances.

* ECR: to push a repository that auths with docker, might need to delete
  `~/.docker/config.json`

```

# auth for private ECR

aws ecr get-login-password --profile 311crimemap --region us-east-2 | \
    docker login --username AWS --password-stdin \
    058264272856.dkr.ecr.us-east-2.amazonaws.com

```

* Don't think K8s repulls unless version tag changes; even if different hash.


### Quickstart Runtime

1. Run `./docker_ops.sh SSH_KEY=<path/to/ssh_key> ENV=<env> ENV_ID=<env id> CMD=bash`
   * `./docker_ops.sh SSH_KEY=/root/.ssh/id_rsa ENV=dev ENV_ID=1 CMD=bash`
2. `cd /infra/<packer|terraform|ansible>`

NB: `SSH_KEY` likely has suffix `**-hetzner`.

#### Packer

`packer init ubuntu.pkr.hcl`

Validate and build image

`packer validate ubuntu.pkr.hcl`

`packer build ubuntu.pkr.hcl`

See [Packer](./infra/packer) for details, information re: using vagrant.

#### Terraform

Create environment and deploy:

```
cd /infra/terraform/environments
mkdir -p <env>/<id>

terraform init

terraform apply

```


#### Ansible

Labels are set in terraform; `type=server`, `type=app`
```

cd /infra/ansible

# spin up
ansible-playbook -e env_id=dev_1 -i hcloud.yml playbooks/site.yml

# uninstall
ansible-playbook -e env_id=dev_1 -i hcloud.yml playbooks/uninstall.yml
```



#### k3s

Once ansible has spun up a k3s cluster, need to copy config to make `kubectl`
available on local/control machine.

Because we need ECR (access to AWS private repositories), need to either:

1. Remote Control Node: install aws cli and setup ~/.aws/credentials chain

2. Local Control Node:
   * download the `~./kube/config` file from control node and run `kubectl`
   * generate secret via "safer", already installed local aws credentials

Currently doing local control node - easier to regenerate ECR token.


```
# 1. Get (copy) IP for node-0 equilalent (available in ansible output)

# 2. copy over kube config

scp root@<node-0-ip / host>:~/.kube/config ~/.kube/config

# 3. Change cluster.server ip from 127.0.0.1 -> <node-0-ip>

nano ~/.kube/config  # change ip

# 4. add KUBECONFIG env

export KUBECONFIG=~/.kube/config

```

#### k3s - node label

* Ansible loops and applies hetzner labels (set by terraform)

#### ssh

Hetzner ubuntu uses default `root` login.

##### k3s cluster run

For example demos, see
[https://github.com/vergeman/k3s-demo](https://github.com/vergeman/k3s-demo)
repository.


