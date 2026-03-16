packer {
    required_plugins {
        # https://developer.hashicorp.com/packer/integrations/hetznercloud/hcloud#builders
        hcloud = {
            source  = "github.com/hetznercloud/hcloud"
            version = "~> 1"
        }
        ansible = {
            version = "~> 1"
            source = "github.com/hashicorp/ansible"
        }
    }
}

variable "ssh_username" {
  type = string
}

variable "ssh_keys" {
  type = list(string)
}

source "hcloud" "basic_ubuntu" {
    image       = "ubuntu-24.04"
    location    = "fsn1"
    server_type = "cx23"

    # resulting image description (not name)
    # name is reserved for base OS images - so have to use labels
    # duplicate names do not overwrite, will raise error
    snapshot_name = "packer_base_311crimemap_2.0"
    snapshot_labels = {
      "name": "packer_base_311crimemap_2.0"
    }

    ssh_username = var.ssh_username
    ssh_keys     = var.ssh_keys
}

build {
    sources = [
        "source.hcloud.basic_ubuntu"
    ]

    provisioner "shell" {
        script = "../scripts/setup.sh"
    }
}
