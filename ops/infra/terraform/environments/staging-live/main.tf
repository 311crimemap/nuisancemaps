# main.tf

module "globals" {
  source ="../../globals"

  # ENV variables available only at root (executing) level
  hcloud_token = var.HCLOUD_TOKEN
}

module "firewall" {
  source = "../../modules/firewall"

  product = var.product
  env = var.env
  env_group = var.env_group
  org_id = "web"

  location_zone = {
    location: "hil",
    network_zone: "us-west"
  }

  http_source_ips = [
#    "0.0.0.0/0",
#    "::/0",
    "10.0.0.0/8"
  ]

  https_source_ips = [
#    "0.0.0.0/0",
#    "::/0",
    "10.0.0.0/8"
  ]
}

module "app-servers" {
  source = "../../modules/app"

  # define variables to pass into this module
  product = var.product
  env = var.env
  env_group = var.env_group
  org_id = "web"
  class_id = "app"

  location_zone = {
    location: "hil",
    network_zone: "us-west"
  }

  server_type = "cpx11"
  server_count = 2
  ssh_key_name = var.ssh_key_name

  image_name = "name=packer_base_311crimemap_1.0"

  additional_labels = {}

  network_name = "network-${var.env}-${var.env_group}"
  firewall_id = module.firewall.hcloud_firewall_id
}

