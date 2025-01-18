# main.tf
#
#
# https://docs.hetzner.com/cloud/general/locations/#what-locations-are-there
# location_zone:
#  hil / us-west
#  nbg1 / eu-central
#

#
module "globals" {
  source ="../../globals"

  # ENV variables available only at root (executing) level
  hcloud_token = var.HCLOUD_TOKEN
  cloudflare_api_token = var.CLOUDFLARE_API_TOKEN
}

module "network" {
  source ="../../modules/network"

  network_name = "network-${var.env}-${var.env_group}"

  location_zone = {
    location: "nbg1",
    network_zone: "eu-central"
  }

}

#
# k3s control server
# metrics (e.g. grafana) and other misc
#
module "control-servers" {
  source = "../../modules/app"

  # define variables to pass into this module
  product = var.product
  env = var.env
  env_group = var.env_group
  org_id = "web"
  class_id = "cntrl"

  location_zone = {
    location: "nbg1",
    network_zone: "eu-central"
  }

  server_type = "cpx21"
  server_count = 1
  ssh_key_name = var.ssh_key_name

  image_name = "name=packer_base_311crimemap_1.0"

  additional_labels = {}
  k3s_server = true

  network_id   = module.network.hcloud_network_id
  network_subnet_id = module.network.hcloud_network_subnet_id
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
    location: "nbg1",
    network_zone: "eu-central"
  }

  server_type = "cpx21"
  server_count = 0
  ssh_key_name = var.ssh_key_name

  image_name = "name=packer_base_311crimemap_1.0"

  additional_labels = {
    "enablelb"="true"
  }

  k3s_server = false

  network_id   = module.network.hcloud_network_id
  network_subnet_id = module.network.hcloud_network_subnet_id
}

module "worker-servers" {
  source = "../../modules/app"

  # define variables to pass into this module
  product = var.product
  env = var.env
  env_group = var.env_group
  org_id = "web"
  class_id = "worker"

  location_zone = {
    location: "nbg1",
    network_zone: "eu-central"
  }

  # min cpx21 needed to parse xls
  server_type = "cpx21"
  server_count = 1
  ssh_key_name = var.ssh_key_name

  image_name = "name=packer_base_311crimemap_1.0"

  additional_labels = {}
  k3s_server = false # workers are set to agent - otherwise need to adjust ansible

  network_id   = module.network.hcloud_network_id
  network_subnet_id = module.network.hcloud_network_subnet_id
}

module "db-servers" {
  source = "../../modules/db"

  # define variables to pass into this module
  product = var.product
  env = var.env
  env_group = var.env_group
  org_id = "web"
  class_id = "db"

  location_zone = {
    location: "nbg1",
    network_zone: "eu-central"
  }

  #
  # first server primary (db-0)
  # rest are replica (db-N) where n > 0
  #
  primary_server_type = "cpx31"
  replica_server_type = "cpx21"
  server_count = 1

  ssh_key_name = var.ssh_key_name

  image_name = "name=packer_base_311crimemap_1.0"

  additional_labels = {
    # for min configuration, but disable if app-servers exist
    "enablelb"="true"
  }
  k3s_server = false

  network_id   = module.network.hcloud_network_id
  network_subnet_id = module.network.hcloud_network_subnet_id
}


module "firewall" {
  source = "../../modules/firewall"

  product = var.product
  env = var.env
  env_group = var.env_group
  org_id = "web"

  location_zone = {
    location: "nbg1",
    network_zone: "eu-central"
  }

  http_source_ips = [
    "10.0.0.0/8"
  ]

  https_source_ips = [
    "10.0.0.0/8"
  ]

  server_ips = flatten(compact(concat(module.control-servers.ipv4_address,
    module.app-servers.ipv4_address,
    module.worker-servers.ipv4_address,
    module.db-servers.ipv4_address
    )))
}
