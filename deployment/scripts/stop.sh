#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/deploy-config.sh"

if [[ -f "$CONFIG_FILE" ]]; then
    echo "📁 Loading configuration from $CONFIG_FILE"
    source "$CONFIG_FILE"
else
    echo "❌ Configuration file not found: $CONFIG_FILE"
    exit 1
fi

stop_droplet1() {    
    echo "🔐 Setting up SSH agent..."
    eval $(ssh-agent -s)
    ssh-add "${SSH_KEY_PATH}"

    echo "🔑 Testing SSH connection..."
    if ! ssh -o BatchMode=yes -o ConnectTimeout=5 -i "${SSH_KEY_PATH}" ${DROPLET1_USER}@${DROPLET1_IP} "echo '✅ SSH Connection successful'"; then
        echo "❌ SSH Connection failed"
        ssh-agent -k
        exit 1
    fi

    echo "Stopping Droplet 1 services..."
    ssh -i "${SSH_KEY_PATH}" ${DROPLET1_USER}@${DROPLET1_IP} << 'ENDSSH'
    cd /opt/stockfellow
    docker-compose -f droplet1-infrastructure.yml down
ENDSSH

    trap "ssh-agent -k" EXIT
}

stop_droplet2() {    
    echo "🔐 Setting up SSH agent..."
    eval $(ssh-agent -s)
    ssh-add "${SSH_KEY_PATH}"

    echo "🔑 Testing SSH connection..."
    if ! ssh -o BatchMode=yes -o ConnectTimeout=5 -i "${SSH_KEY_PATH}" ${DROPLET2_USER}@${DROPLET2_IP} "echo '✅ SSH Connection successful'"; then
        echo "❌ SSH Connection failed"
        ssh-agent -k
        exit 1
    fi

    echo "Stopping Droplet 2 services..."
    ssh -i "${SSH_KEY_PATH}" ${DROPLET2_USER}@${DROPLET2_IP} << 'ENDSSH'
    cd /opt/stockfellow
    docker-compose -f droplet2-gateway.yml down
ENDSSH

    trap "ssh-agent -k" EXIT
}

stop_droplet3() {    
    echo "🔐 Setting up SSH agent..."
    eval $(ssh-agent -s)
    ssh-add "${SSH_KEY_PATH}"

    echo "🔑 Testing SSH connection..."
    if ! ssh -o BatchMode=yes -o ConnectTimeout=5 -i "${SSH_KEY_PATH}" ${DROPLET3_USER}@${DROPLET3_IP} "echo '✅ SSH Connection successful'"; then
        echo "❌ SSH Connection failed"
        ssh-agent -k
        exit 1
    fi

    echo "Stopping Droplet 3 services..."
    ssh -i "${SSH_KEY_PATH}" ${DROPLET3_USER}@${DROPLET3_IP} << 'ENDSSH'
    cd /opt/stockfellow
    docker-compose -f droplet3-services.yml down
ENDSSH

    trap "ssh-agent -k" EXIT
}

case "$1" in
    1)
        stop_droplet1
        ;;
    2)
        stop_droplet2
        ;;
    3)
        stop_droplet3
        ;;
    *)
        echo "Usage: $0 {1|2|3}"
        exit 1
        ;;
esac