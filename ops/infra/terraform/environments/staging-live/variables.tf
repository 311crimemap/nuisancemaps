# variables.tf
#
# any var.* usage in root level accompanying main.tf

variable HCLOUD_TOKEN {
  type = string
  description = "hetzner token set by env variable prefix (in .env file) - TF_VAR_HCLOUD_TOKEN"
}

variable CLOUDFLARE_API_TOKEN {
  type = string
  description = "Cloudflare token set by env variable prefix (in .env file) - TF_VAR_CLOUDFLARE_API_TOKEN"
}

variable ssh_key_name {
  type = string
  description = "name of ssh key on hetzner"
}

variable product {
  type = string
  description = "umbrella product name"
}

variable env {
  type = string
  description = "environment group e.g. 'dev'"
}

variable env_group {
  type = string
  description = "environment group 'live', '1', etc."
}

