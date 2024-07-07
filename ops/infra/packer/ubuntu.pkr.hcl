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

source "hcloud" "basic_ubuntu" {
    image       = "ubuntu-22.04"
    location    = "hil"
    server_type = "cpx11"

    # resulting image description (not name)
    # name is reserved for base OS images - so have to use labels
    # duplicate names do not overwrite, will raise error
    snapshot_name = "packer_base_311crimemap_1.0"
    snapshot_labels = {
      "name": "packer_base_311crimemap_1.0"
    }

    ssh_username = "root"
    ssh_keys     = ["admin@311crimemap.com"]
}

build {
    sources = [
        "source.hcloud.basic_ubuntu"
    ]

    provisioner "shell" {
        script = "./scripts/setup.sh"
    }

    # provisioner "ansible" {
    #     playbook_file = "/ops/ansible/ansible-playbook-packer.yml"
    #     ansible_env_vars = [
    #         "ANSIBLE_HOST_KEY_CHECKING=False",
    #         "SSH_AUTH_SOCK=/ssh-agent"
    #     ]
    # }
}
