# variables.tf

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

variable location_zone {
  type = map(string)
  default = {
    location: "hil",
    network_zone: "us-west"
  }
  description = "location for instance and accompanying network_zone for network resource."
}
