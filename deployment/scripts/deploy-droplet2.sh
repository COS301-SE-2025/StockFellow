#!/bin/bash
# deploy-droplet2.sh - Gateway Services

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
if ! ssh -o BatchMode=yes -o ConnectTimeout=5 -i "${SSH_KEY_PATH}" ${DROPLET2_USER}@${DROPLET2_IP} "echo 'SSH Connection successful'"; then
    echo "SSH Connection failed"
    exit 1
fi

echo "Deploying Gateway Services to Droplet 2 (${DROPLET2_IP})..."

ssh ${DROPLET2_USER}@${DROPLET2_IP} "mkdir -p /opt/stockfellow"

# scp -i ${SSH_KEY_PATH} /mnt/c/Users/deanr/Documents/University/2025/COS301/Capstone/StockFellow/deployment/compose/droplet2-gateway.yml ${DROPLET2_USER}@${DROPLET2_IP}:/opt/stockfellow/
# scp -i ${SSH_KEY_PATH} /mnt/c/Users/deanr/Documents/University/2025/COS301/Capstone/StockFellow/deployment/scripts/.env.droplet2 ${DROPLET2_USER}@${DROPLET2_IP}:/opt/stockfellow/.env
# scp -i ${SSH_KEY_PATH} droplet2-gateway.yml ${DROPLET2_USER}@${DROPLET2_IP}:/opt/stockfellow/
# scp -i ${SSH_KEY_PATH} .env.droplet2 ${DROPLET2_USER}@${DROPLET2_IP}:/opt/stockfellow/.env
# scp -i ${SSH_KEY_PATH} -r services/api-gateway/ ${DROPLET2_USER}@${DROPLET2_IP}:/opt/stockfellow/
# scp -i ${SSH_KEY_PATH} -r services/api-gateway/realm-exports/ ${DROPLET2_USER}@${DROPLET2_IP}:/opt/stockfellow/
scp -i ${SSH_KEY_PATH} -r /mnt/c/Users/deanr/Documents/University/2025/COS301/Capstone/StockFellow/integrations/keycloak-extensions/ ${DROPLET2_USER}@${DROPLET2_IP}:/opt/stockfellow/

# rsync -av --exclude='target/' --exclude='build/' --exclude='.git/' --exclude='*.log' --exclude='node_modules/' --exclude='test/' -e "ssh -i ${SSH_KEY_PATH}" /mnt/c/Users/deanr/Documents/University/2025/COS301/Capstone/StockFellow/services/mfa-service ${DROPLET2_USER}@${DROPLET2_IP}:/opt/stockfellow/mfa-service

# ssh -i ${SSH_KEY_PATH} ${DROPLET2_USER}@${DROPLET2_IP} << 'ENDSSH'
# cd /opt/stockfellow
# docker-compose -f droplet2-gateway.yml down
# docker-compose -f droplet2-gateway.yml build
# docker-compose -f droplet2-gateway.yml up -d
# docker-compose -f droplet2-gateway.yml ps
# ENDSSH

trap "ssh-agent -k" EXIT

echo "Droplet 2 deployment completed"


