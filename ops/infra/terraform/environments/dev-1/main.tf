# main.tf

module "globals" {
  source ="../../globals"

  # ENV variables available only at root (executing) level
  hcloud_token = var.HCLOUD_TOKEN
}

module "firewall" {
  source = "../../modules/firewall"
}

module "app-servers" {
  source = "../../modules/app"

  # define variables to pass into this module

  server_type = "cpx11"
  server_count = 2
  ssh_key_name = var.ssh_key_name

  location_zone = {
    location: "hil",
    network_zone: "us-west"
  }

  image_name = "name=packer_base_311crimemap_1.0"

  labels =  {
    "server": "app"
  }

  firewall_id = module.firewall.hcloud_firewall_id
}

