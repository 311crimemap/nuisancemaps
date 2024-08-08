#main.tf

resource "hcloud_network" "network" {
  name     = var.network_name
  ip_range = "10.0.0.0/16"
}


resource "hcloud_network_subnet" "network-subnet" {
  network_id   = hcloud_network.network.id
  type         = "cloud"
  network_zone = var.location_zone.network_zone
  ip_range     = "10.0.0.0/24"
}
