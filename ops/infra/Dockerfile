FROM ubuntu:22.04
ENV DEBIAN_FRONTEND noninteractive

#
# PINNED VERSIONS
#
ARG PYTHON_VERSION=python3.10      # force by ansible version
ARG ANSIBLE_VERSION=9.1.0          # installs dependency ansible core 2.16
ARG HCLOUD_VERSION=1.33.2          # dynamic inventory hetzner - installs requests lib
ARG TERRAFORM_VERSION=1.6.6-1
ARG PACKER_VERSION=1.10.0-1

RUN apt-get update && \
    apt-get install -y software-properties-common apt-utils \
    curl \
    gnupg \
    ca-certificates \
    # ansible requirements
    openssh-server rsync \
    # utilities
    jq git


#
# ADD REPOSITORIES
#

# Python & Ansible

RUN add-apt-repository --yes ppa:deadsnakes/ppa && \
    add-apt-repository --yes ppa:ansible/ansible

# Terraform & Packer

RUN curl -fsSL https://apt.releases.hashicorp.com/gpg | apt-key add - && \
    add-apt-repository --yes "deb [arch=amd64] https://apt.releases.hashicorp.com $(lsb_release -cs) main"

RUN apt-get update && \
    apt-get install -y $PYTHON_VERSION python3-pip

#
# install pip packages
#

RUN pip3 install ansible==$ANSIBLE_VERSION \
                 hcloud==$HCLOUD_VERSION

RUN apt-get install -y terraform=$TERRAFORM_VERSION \
                       packer=$PACKER_VERSION


# add a symlink since dynamic inventory seems to only call 'python'

RUN ln -s /usr/bin/$PYTHON_VERSION /usr/bin/python && \
    ansible --version | grep "python version"

CMD ["/bin/bash"]
