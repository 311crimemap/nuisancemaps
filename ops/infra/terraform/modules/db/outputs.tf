# outputs.tf
#server_ids

output "ipv4_address" {
  value = hcloud_server.db.*.ipv4_address
}

output "ipv6_address" {
  value = hcloud_server.db.*.ipv6_address
}
