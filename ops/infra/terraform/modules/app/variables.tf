# main.tf variables

variable server_type {
  type = string
  description = "server class / instance type on hetzner"
  #
  # | name  | vCPU | RAM | SSD | Traffic | Price |
  # | --------------------------------------------
  # | cpx11 |  2   |  2  |  40 |   20    |  3.85 |
  # | cpx21 |  3   |  4  |  80 |   20    |  7.05 |
  # | cpx31 |  4   |  8  | 160 |   20    | 13.10 |
  # | cpx41 |  8   |  16 | 240 |   20    | 24.70 |
  #
}

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

variable labels {
  type = map(string)
  description = "key-value pairs of labels to assign to each node, with default type to denote k3s server or agent"
}

variable ssh_key_name {
  type = string
  description = "name of ssh key on hetzner"
}

variable image_name {
  type = string
  description = "image snapshot selector expression: e.g. name=packer_image..."
}

variable firewall_id {
  type = number
  description = "firewall id"
}
