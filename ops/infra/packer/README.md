# Packer

## Quickstart

Run `./docker_ops.sh` and `bash` into `/packer`

Install any required plugins:

`packer init ubuntu.pkr.hcl`

Validate and build

`packer validate ubuntu.pkr.hcl`

`packer build ubuntu.pkr.hcl`

### Access

Packer uses API token to access hetzner. To create the image, packer generates
it's own temporary ssh key.

This is not the same as the variable `ssh-key`, which configures the key used by
the image.


### Deprecated Notes

See `hetzner-ops-demo` repo for Vagrant and/or Docker examples (to reduce
spend). But current provisioning is almost entirely delegated to k3s and
individual containers.
