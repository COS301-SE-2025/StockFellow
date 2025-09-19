#!/bin/bash
# This script is used to configure the firewalls on each droplet.
# The script is modified an run on the droplet itself

source $(dirname "$0")/deploy-config.sh

setup_droplet1_firewall() {    
    ufw allow ssh
    
    ufw allow from $DROPLET2_IP to any port 5432
    ufw allow from $DROPLET3_IP to any port 5432
    
    ufw allow from $DROPLET2_IP to any port 6379
    ufw allow from $DROPLET3_IP to any port 6379
    
    ufw allow from $DROPLET3_IP to any port 61616
    
    ufw allow 61208
    
    ufw --force enable
}

setup_droplet2_firewall() {
    ufw allow ssh
    
    ufw allow 80
    ufw allow 443  
    ufw allow 3000  
    ufw allow 8080  
    ufw allow 8087  
    
    ufw allow from $DROPLET3_IP to any port 8080
    
    ufw --force enable
}

setup_droplet3_firewall() {
    ufw allow ssh
    
    ufw allow from $DROPLET2_IP to any port 4020
    ufw allow from $DROPLET2_IP to any port 4040   
    ufw allow from $DROPLET2_IP to any port 4080
    ufw allow from $DROPLET2_IP to any port 4050
    
    ufw --force enable
}

case "$1" in
    1)
        setup_droplet1_firewall
        ;;
    2)
        setup_droplet2_firewall
        ;;
    3)
        setup_droplet3_firewall
        ;;
    *)
        echo "Usage: $0 {1|2|3}"
        exit 1
        ;;
esac