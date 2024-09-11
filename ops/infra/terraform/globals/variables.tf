# provider variables

variable hcloud_token {
  type = string
  description = "hetzner token passed (TF_VAR_ env variable)"
}

variable cloudflare_api_token {
  type = string
  description = "Cloudflare API Token (TF_VAR_ env variable)"
}
