# main.tf variables
variable server_count {
  type = number
  description = "server count"
}

variable location_zone {
  type = map(string)
  default = {
    location: "hil",
    network_zone: "us-west"
  }
  description = "location for instance and accompanying network_zone for network resource."
}

variable ssh_key_name {
  type = string
  description = "name of ssh key on hetzner"
}

variable firewall_id {
  type = number
  description = "firewall id"
}
