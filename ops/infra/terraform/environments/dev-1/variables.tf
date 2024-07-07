# variables.tf
#
# any var.* usage in root level accompanying main.tf

variable HCLOUD_TOKEN {
  type = string
  description = "hetzner token set by env variable prefix (in .env file) - TF_VAR_HCLOUD_TOKEN"
}

variable ssh_key_name {
  type = string
  description = "name of ssh key on hetzner"
}
