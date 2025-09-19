#!/bin/bash
# deploy-droplet3.sh - Business Services Deployment

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

echo "Deploying Business Services to Droplet 3..."

ssh ${DROPLET3_USER}@${DROPLET3_IP} "mkdir -p /opt/stockfellow"

scp -i ${SSH_KEY_PATH} droplet3-services.yml ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/
scp -i ${SSH_KEY_PATH} .env.droplet3 ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/.env
scp -i ${SSH_KEY_PATH} -r services/user-service/ ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/
scp -i ${SSH_KEY_PATH} -r services/group-service/ ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/
scp -i ${SSH_KEY_PATH} -r services/transaction-service/ ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/
scp -i ${SSH_KEY_PATH} -r services/notification-service/ ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/

ssh -i ${SSH_KEY_PATH} ${DROPLET3_USER}@${DROPLET3_IP} << 'ENDSSH'
cd /opt/stockfellow
docker-compose -f droplet3-services.yml down
docker-compose -f droplet3-services.yml build
docker-compose -f droplet3-services.yml up -d
docker-compose -f droplet3-services.yml ps
ENDSSH

echo "Droplet 3 deployment completed"