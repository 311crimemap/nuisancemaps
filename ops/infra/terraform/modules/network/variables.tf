# variables.tf

variable network_name {
  type = string
  description = "network name"
}

variable location_zone {
  type = map(string)
  default = {
    location: "hil",
    network_zone: "us-west"
  }
  description = "location for instance and accompanying network_zone for network resource."
}
