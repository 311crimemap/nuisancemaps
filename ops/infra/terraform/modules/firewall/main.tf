
data "cloudflare_ip_ranges" "cloudflare" {}

data "http" "bastion_ip" {
  url = "https://api.ipify.org?format=text"
}

resource "hcloud_firewall" "firewall-311crimemap" {

  name = "firewall-${var.product}-${var.env}-${var.env_group}-${var.org_id}-${var.location_zone.location}-${var.location_zone.network_zone}"


  apply_to {
    label_selector = "env_id=${var.env}-${var.env_group}"
  }

  rule {
    description = "K3s supervisor and Kubernetes API Server"
    direction = "in"
    protocol  = "tcp"
    port      = "6443"
    source_ips = concat(var.server_ips,
      [
        "10.0.0.0/8",
        data.http.bastion_ip.response_body
      ])
  }

  rule {
    description = "Required only for Flannel VXLAN"
    direction = "in"
    protocol  = "udp"
    port      = "8472"

    source_ips = concat(var.server_ips, ["10.0.0.0/8"])
  }

  rule {
    description = "Kubelet Metrics"
    direction = "in"
    protocol  = "tcp"
    port      = "10250"
    source_ips = [
      "10.0.0.0/8",
      data.http.bastion_ip.response_body
    ]
  }

  rule {
    description = "Required only for Flannel Wireguard with IPv4 and IPv6"
    direction = "in"
    protocol  = "udp"
    port      = "51820-51821"
    source_ips = [
      "10.0.0.0/8"
    ]
  }

  rule {
    description = "Required only for HA with embedded etcd"
    direction = "in"
    protocol  = "tcp"
    port      = "2379-2380"
    source_ips = [
      "10.0.0.0/8"
    ]
  }

  rule {
    description = "ssh"
    direction = "in"
    protocol  = "tcp"
    port      = "22"
    source_ips = [
      "10.0.0.0/8",
      data.http.bastion_ip.response_body
    ]
  }

  #
  # in event of running ssh on secondary port (block)
  #

  # rule {
  #   description = "ssh"
  #   direction = "in"
  #   protocol  = "tcp"
  #   port      = "8080"
  #   source_ips = [
  #     "0.0.0.0/0",
  #     "::/0",
  #     "10.0.0.0/8"
  #   ]
  # }

  rule {
    description = "http"
    direction = "in"
    protocol  = "tcp"
    port      = "80"
    source_ips = concat(var.http_source_ips,
      data.cloudflare_ip_ranges.cloudflare.ipv4_cidr_blocks,
      data.cloudflare_ip_ranges.cloudflare.ipv6_cidr_blocks)

  }

  rule {
    description = "https"
    direction = "in"
    protocol  = "tcp"
    port      = "443"
    source_ips = concat(var.https_source_ips,
      data.cloudflare_ip_ranges.cloudflare.ipv4_cidr_blocks,
      data.cloudflare_ip_ranges.cloudflare.ipv6_cidr_blocks)
  }


  depends_on = [
    var.server_ips
  ]
}
