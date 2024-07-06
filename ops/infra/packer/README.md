# Packer

## Quickstart

Run `./docker_ops.sh` and `bash` into `/packer`

Install any required plugins:

`packer init ubuntu.pkr.hcl`

Validate and build

`packer validate ubuntu.pkr.hcl`

`packer build ubuntu.pkr.hcl`

### Deprecated Notes

See `hetzner-ops-demo` repo for Vagrant and/or Docker examples (to reduce
spend). But current provisioning is almost entirely delegated to k3s and
individual containers.
