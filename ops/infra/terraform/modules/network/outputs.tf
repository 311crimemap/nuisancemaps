#outputs.tf

output "hcloud_network_id" {
  value = hcloud_network.network.id
}

output "hcloud_network_subnet_id" {
  value = hcloud_network_subnet.network-subnet.id
}
