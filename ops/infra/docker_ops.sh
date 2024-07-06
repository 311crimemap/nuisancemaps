#!/usr/bin/env bash
#
# ./docker_ops.sh production live apply
# ./docker_ops.sh dev 1 apply
#
export TF_LOG=1

DOCKER_IMAGE="058264272856.dkr.ecr.us-east-2.amazonaws.com/311crimemap/ops"
INFRA_DIR="infra"
TERRAFORM_DIR="dev-1"  # ENV + ENV_ID (environment directory)

ENV=$1
if [ "$ENV" != "production" ] && [ "$ENV" != "staging" ] &&  [ "$ENV" != "dev" ] ; then
    echo "missing environment: production, staging"
    echo "example live './docker_ops.sh' production live apply"
    exit 1
fi

ENV_ID=$2
if [ -z "$ENV_ID" ]; then
    echo "missing env_id: live, a, b"
    echo "example live './docker_ops.sh' production live apply"
    exit 1
fi

TERRAFORM_DIR="./terraform/environments/${ENV}-${ENV_ID}/"
if [ ! -d "$TERRAFORM_DIR" ]; then
    echo "$TERRAFORM_DIR does not exist - setup dir:"
    #echo "1. create dir"
    #echo "2. copy over *.tf, *.tfvars"
    #echo "3. run terraform init"
    exit 1
fi

set -x

DEFAULT_CMD=$3

if [ "$DEFAULT_CMD" == "bash" ]; then

    docker run -it \
           -v "$(pwd)/":"/$OPS_DIR/" \
           -v /var/run/docker.sock:/var/run/docker.sock \
           -v $SSH_AUTH_SOCK:/ssh-agent \
           -e "SSH_AUTH_SOCK=/ssh-agent" \
           --net host \
           -w "/$TERRAFORM_DIR" \
           --env-file .env \
           -e "TF_VAR_deploy_env=${ENV}" \
           -e "TF_VAR_env_id=${ENV_ID}" \
           "$DOCKER_IMAGE:latest" \
           $DEFAULT_CMD

    exit 1;
fi

docker run -it \
       -v "$(pwd)/":"/$OPS_DIR/" \
       -v /var/run/docker.sock:/var/run/docker.sock \
       -v $SSH_AUTH_SOCK:/ssh-agent \
       -e "SSH_AUTH_SOCK=/ssh-agent" \
       --net host \
       -w "/$TERRAFORM_DIR" \
       --env-file .env \
       -e "TF_VAR_deploy_env=${ENV}" \
       -e "TF_VAR_env_id=${ENV_ID}" \
       "$DOCKER_IMAGE:latest" \
       terraform $DEFAULT_CMD \
       -var-file="terraform.tfvars"
