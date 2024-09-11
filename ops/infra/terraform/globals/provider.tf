# provider.tf
#
# https://registry.terraform.io/providers/hetznercloud/hcloud/latest/docs#argument-reference
# see "how to use this provider"
#
# https://registry.terraform.io/providers/cloudflare/cloudflare/latest/docs
#

terraform {
  required_providers {
    hcloud = {
      source = "hetznercloud/hcloud"
      version = ">= 1.44.1"
    }
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 4.0"
    }
  }
}

# Configure the Hetzner Cloud Provider
# NB: uses env var passed in environment's main.tf via globals module
provider "hcloud" {
  token = var.hcloud_token
}

# Configure Cloudflare provider for ip whitelist (CF to origin servers)
# NB: uses env var passed in environment's main.tf via globals module
provider "cloudflare" {
  api_token = var.cloudflare_api_token
}
