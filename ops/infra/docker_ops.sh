#!/usr/bin/env bash
#
# ./docker_ops.sh production live apply
# ./docker_ops.sh dev 1 apply
#
export TF_LOG=1

IMAGE_REPO=$(grep -E '^(IMAGE_REPO)' .env | awk -F'=' '{print $2}')
DOCKER_IMAGE="$IMAGE_REPO/311crimemap/ops"
INFRA_DIR="infra"

# Default values
ENV="dev"
ENV_ID=1
SSH_KEY=""
CMD="bash"

# Parse arguments
for arg in "$@"
do
    case $arg in
        SSH_KEY=*)
            SSH_KEY="${arg#*=}"
            ;;
        ENV=*)
            ENV="${arg#*=}"
            ;;
        ENV_ID=*)
            ENV_ID="${arg#*=}"
            ;;
        CMD=*)
            CMD="${arg#*=}"
            ;;

        *)
            echo "Unknown argument: $arg"
            exit 1
            ;;
    esac
done

if [ -z "$SSH_KEY" ]; then
    echo "populate SSH_KEY bash variable with path"
    echo "example: SSH_KEY=/root/.ssh/id_rsa"
    exit 1
fi

if [ "$ENV" != "production" ] && [ "$ENV" != "staging" ] &&  [ "$ENV" != "dev" ] ; then
    echo "missing environment: production, staging"
    echo "example live './docker_ops.sh' SSH_KEY=/path/to/key ENV=production ENV_ID=live CMD=bash"
    exit 1
fi

if [ -z "$ENV_ID" ]; then
    echo "missing env_id: 1, live, a, b"
    echo "example live './docker_ops.sh' SSH_KEY=/path/to/key ENV=production ENV_ID=live CMD=bash"
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

docker run -it \
       -v $SSH_KEY:"/root/.ssh/id_rsa" \
       -v "$(pwd)/":"/$INFRA_DIR/" \
       --net host \
       -w "/$INFRA_DIR/$TERRAFORM_DIR" \
       --env-file .env \
       -e "TF_VAR_deploy_env=${ENV}" \
       -e "TF_VAR_env_id=${ENV_ID}" \
       "$DOCKER_IMAGE:latest" \
       $CMD
