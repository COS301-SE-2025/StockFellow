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

echo "Setting up SSH agent..."
eval $(ssh-agent -s)

ssh-add "${SSH_KEY_PATH}"

echo "Testing SSH connection..."
if ! ssh -o BatchMode=yes -o ConnectTimeout=5 -i "${SSH_KEY_PATH}" ${DROPLET3_USER}@${DROPLET3_IP} "echo 'SSH Connection successful'"; then
    echo "SSH Connection failed"
    exit 1
fi

echo "Deploying Business Services to Droplet 3 (${DROPLET3_IP})..."

# ssh ${DROPLET3_USER}@${DROPLET3_IP} "mkdir -p /opt/stockfellow"
# ssh ${DROPLET3_USER}@${DROPLET3_IP} "mkdir -p /opt/stockfellow/apps"

# scp -i ${SSH_KEY_PATH} ./deployment/compose/droplet3-services.yml ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/
scp -i ${SSH_KEY_PATH} ./deployment/scripts/.env.droplet3 ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/.env

# rsync -av --exclude='target/' --exclude='build/' --exclude='.git/' --exclude='*.log' --exclude='node_modules/' --exclude='test/' -e "ssh -i ${SSH_KEY_PATH}" services/user-service/ ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/user-service/
# rsync -av --exclude='target/' --exclude='build/' --exclude='.git/' --exclude='*.log' --exclude='node_modules/' --exclude='test/' -e "ssh -i ${SSH_KEY_PATH}" services/group-service/ ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/group-service/
# rsync -av --exclude='target/' --exclude='build/' --exclude='.git/' --exclude='*.log' --exclude='node_modules/' --exclude='test/' -e "ssh -i ${SSH_KEY_PATH}" services/transaction-service/ ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/transaction-service/
# rsync -av --exclude='target/' --exclude='build/' --exclude='.git/' --exclude='*.log' --exclude='node_modules/' --exclude='test/' -e "ssh -i ${SSH_KEY_PATH}" services/notification-service/ ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/notification-service/
rsync -av --exclude='target/' --exclude='build/' --exclude='.git/' --exclude='*.log' --exclude='node_modules/' --exclude='test/' -e "ssh -i ${SSH_KEY_PATH}" services/admin-service/ ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/admin-service/
# rsync -av --exclude='target/' --exclude='build/' --exclude='.git/' --exclude='*.log' --exclude='node_modules/' --exclude='test/' -e "ssh -i ${SSH_KEY_PATH}" apps/web-app/ ${DROPLET3_USER}@${DROPLET3_IP}:/opt/stockfellow/apps/web-app/


# ssh -i ${SSH_KEY_PATH} ${DROPLET3_USER}@${DROPLET3_IP} << 'ENDSSH'
# cd /opt/stockfellow
# docker-compose -f droplet3-services.yml down
# docker-compose -f droplet3-services.yml build
# docker-compose -f droplet3-services.yml up -d
# docker-compose -f droplet3-services.yml ps
# ENDSSH

trap "ssh-agent -k" EXIT

echo "Droplet 3 deployment completed"