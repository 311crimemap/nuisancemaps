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

# note underscore in env, this is forced by ansible

ansible-playbook -e env_id=dev_1 -i hcloud.yml playbooks/site.yml

ansible-playbook -e env_id=dev_1 -i hcloud.yml playbooks/uninstall.yml
```

On local machine, just scp and export KUBECONFIG to `~/.kube/config` to run
kubectl.

### Labels

Current `hcloud.yml` dynamic inventory creates group labels via "keyed_groups".

The format is `label_<key>_<value>`.

For example the groups distinguishing between server and agent nodes are labeled
on Hetzner via terraform as `node=server`, `node=agent`. Ansible will generate
the groups, `label_node_server`, `label_node_agent`, which contain the
respective hostnames.

We can see other grouped labels using `debug.yml`:

`ansible-playbook -i hcloud.yml debug.yml`

NB: Ansible automatically converts hyphens to underscore in label names (e.g.
`dev-1` must be referrred to as `dev_1`).

Current playbook has k3s installed on intersection of specified environment
(`label_env_id_<env_id>`) and `label_node_server` / `label_node_agent`.


* `node=server`
* `node=agent`



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
