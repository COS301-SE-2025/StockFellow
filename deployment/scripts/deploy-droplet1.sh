#!/bin/bash
# deploy-droplet1.sh - Infrastructure Services

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/deploy-config.sh"

if [[ -f "$CONFIG_FILE" ]]; then
    echo "Loading configuration from $CONFIG_FILE"
    source "$CONFIG_FILE"
else
    echo "Configuration file not found: $CONFIG_FILE"
    exit 1
fi

echo "Setting up SSH agent..."
eval $(ssh-agent -s)

ssh-add "${SSH_KEY_PATH}"

echo "Testing SSH connection..."
if ! ssh -o BatchMode=yes -o ConnectTimeout=5 -i "${SSH_KEY_PATH}" ${DROPLET1_USER}@${DROPLET1_IP} "echo 'SSH Connection successful'"; then
    echo "SSH Connection failed"
    exit 1
fi

echo "Deploying Infrastructure Services to Droplet 1 (${DROPLET1_IP})..."

ssh -i "${SSH_KEY_PATH}" ${DROPLET1_USER}@${DROPLET1_IP} "mkdir -p /opt/stockfellow"

scp -i "${SSH_KEY_PATH}" ./deployment/compose/droplet1-infrastructure.yml ${DROPLET1_USER}@${DROPLET1_IP}:/opt/stockfellow/
scp -i "${SSH_KEY_PATH}" ./deployment/scripts/.env.droplet1 ${DROPLET1_USER}@${DROPLET1_IP}:/opt/stockfellow/.env
scp -i "${SSH_KEY_PATH}" -r ./deployment/database/ ${DROPLET1_USER}@${DROPLET1_IP}:/opt/stockfellow/

ssh -i "${SSH_KEY_PATH}" ${DROPLET1_USER}@${DROPLET1_IP} << ENDSSH
cd /opt/stockfellow

# Update env vars
sed -i "s/POSTGRES_PASSWORD=.*/POSTGRES_PASSWORD=${POSTGRES_PASSWORD}/" .env
sed -i "s/REDIS_PASSWORD=.*/REDIS_PASSWORD=${REDIS_PASSWORD}/" .env

# Deploy services
docker-compose -f droplet1-infrastructure.yml down
docker-compose -f droplet1-infrastructure.yml pull
docker-compose -f droplet1-infrastructure.yml up -d
docker-compose -f droplet1-infrastructure.yml ps
ENDSSH

trap "ssh-agent -k" EXIT

echo "Droplet 1 deployment completed"