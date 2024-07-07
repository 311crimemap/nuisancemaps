# Ansible

Currently using the basic install script to download and configure k3s on each
node.

Overall goal with ansible is to create a k3s cluster.

Once cluster is running:

* download kube config to local/control machine (e.g `scp root@<host>:~/.kube/config`)
* `export KUBECONFIG` to set environment
* `kubectl deploy` ... manually deploy


Directory Contents:

* `site.yml`: main playbook to install k3s and join a cluster. Currently single
  server node and N agents.
* `uninstall.yml`: uninstalls k3s on each node

To Run, get into ops container environment: `./docker_ops.sh dev 1 bash`

```
eval "$(ssh-agent -s)"

# in docker_ops container
cd /ops/ansible

ansible-playbook -i hcloud.yml playbooks/site.yml

ansible-playbook -i hcloud.yml playbooks/uninstall.yml
```

On local machine, just scp and export KUBECONFIG to `~/.kube/config` to run
kubectl.

### Labels

Current `hcloud.yml` dynamic inventory has a labels "keyed_groups" as an
inventory grouping to distinguish between server and agent nodes for k3s.

These are set by terraform at the physical machine level. Currently:

* `type=server`
* `type=agent`


##### ansible.cfg

Sets up runtime config:

* `roles-path`: `/roles`
* `inventory`: uses dynamic provider `hcloud.yml`
* `host_key_checking`: mute the ssh warning

##### vars.yml

Used during playbook runtime to specify variables (directory paths, version
etc.)

##### HA

Currently skipping HA configuration. See
https://github.com/k3s-io/k3s-ansible/blob/master/roles/k3s_server/tasks/main.yml
for HA ansible tasks.
