resource "hcloud_firewall" "firewall-311crimemap" {
  name = "firewall-311crimemap"

  rule {
    description = "K3s supervisor and Kubernetes API Server"
    direction = "in"
    protocol  = "tcp"
    port      = "6443"
    source_ips = [
      "0.0.0.0/0",
      "::/0",
      "10.0.0.0/8"
    ]
  }

  rule {
    description = "Required only for Flannel VXLAN"
    direction = "in"
    protocol  = "udp"
    port      = "8472"
    source_ips = [
      "0.0.0.0/0",
      "::/0",
      "10.0.0.0/8"
    ]
  }

  rule {
    description = "Kubelet Metrics"
    direction = "in"
    protocol  = "tcp"
    port      = "10250"
    source_ips = [
      "0.0.0.0/0",
      "::/0",
      "10.0.0.0/8"
    ]
  }

  rule {
    description = "Required only for Flannel Wireguard with IPv4 and IPv6"
    direction = "in"
    protocol  = "udp"
    port      = "51820-51821"
    source_ips = [
      "0.0.0.0/0",
      "::/0",
      "10.0.0.0/8"
    ]
  }

  rule {
    description = "Required only for HA with embedded etcd"
    direction = "in"
    protocol  = "tcp"
    port      = "2379-2380"
    source_ips = [
      "0.0.0.0/0",
      "::/0",
      "10.0.0.0/8"
    ]
  }

  rule {
    description = "ssh"
    direction = "in"
    protocol  = "tcp"
    port      = "22"
    source_ips = [
      "0.0.0.0/0",
      "::/0",
      "10.0.0.0/8"
    ]
  }

  rule {
    description = "ssh"
    direction = "in"
    protocol  = "tcp"
    port      = "8080"
    source_ips = [
      "0.0.0.0/0",
      "::/0",
      "10.0.0.0/8"
    ]
  }


}

