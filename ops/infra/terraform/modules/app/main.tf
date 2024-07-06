# app main.tf

resource "hcloud_network" "network" {
  name     = "network"
  ip_range = "10.0.0.0/16"
}
resource "hcloud_network_subnet" "network-subnet" {
  network_id   = hcloud_network.network.id
  type         = "cloud"
  network_zone = var.location_zone.network_zone
  ip_range     = "10.0.0.0/24"
}

resource "hcloud_server" "app" {
  count       = var.server_count
  name        = "node-${count.index}"
  server_type = "cpx11"

  # image uses id when referring to snapshot
  # or "ubuntu22.04" name for base OS image
  #  image       = "ubuntu-22.04"
  image       = data.hcloud_image.packer.id

  location    = var.location_zone.location

  ssh_keys = [data.hcloud_ssh_key.ssh_key.id]
  firewall_ids = [var.firewall_id]

  public_net {
    ipv4_enabled = true
    ipv6_enabled = true
  }

  network {
    network_id = hcloud_network.network.id
    # ip         = "10.0.1.5"
    # alias_ips  = [
    #   "10.0.1.6",
    #   "10.0.1.7"
    # ]
  }

  labels = {
    "server": "app",
    "test-key" : "test-value",
    "type" :  count.index == 0 ? "server" : "agent"
  }

  depends_on = [
    hcloud_network_subnet.network-subnet
  ]
}

data "hcloud_image" "packer" {
  # NB: cannot use name - reserved
  # id = 143348468

  with_selector = "name=packer_snapshot_2"
  most_recent = true
}

data "hcloud_ssh_key" "ssh_key" {
  name = var.ssh_key_name
}
