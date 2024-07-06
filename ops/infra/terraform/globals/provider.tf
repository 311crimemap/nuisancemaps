# provider.tf
#
# see https://registry.terraform.io/providers/hetznercloud/hcloud/latest/docs#argument-reference
# "how to use this provider"

terraform {
  required_providers {
    hcloud = {
      source = "hetznercloud/hcloud"
      version = ">= 1.44.1"
    }
  }
}

# Configure the Hetzner Cloud Provider
provider "hcloud" {
  token = var.hcloud_token
}
