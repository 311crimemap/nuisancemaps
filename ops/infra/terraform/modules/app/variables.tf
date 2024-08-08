# main.tf variables

variable product {
  type = string
  description = "umbrella product name"
}

variable env {
  type = string
  description = "environment group e.g. 'dev'"
}

variable env_group {
  type = string
  description = "environment group 'live', '1', etc. Combines with env + env_group to generate env_id"
}

variable org_id {
  type = string
  description = "org name e.g 'web'"
}

variable class_id {
  type = string
  description = "server class / use: e.g. app, db, etc"
}

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

variable additional_labels {
  type = map(string)
  description = "key-value pairs of labels to assign to each node"
}

variable ssh_key_name {
  type = string
  description = "name of ssh key on hetzner"
}

variable image_name {
  type = string
  description = "image snapshot selector expression: e.g. name=packer_image..."
}

variable k3s_server {
  type = bool
  description = "whether the first node of this module should be a k3s server"
}

variable network_id {
  type = number
  description = "hcloud network id"
}

variable network_subnet_id {
  type = string
  description = "hcloud network subnet id"
}

variable firewall_id {
  type = number
  description = "firewall id"
}
