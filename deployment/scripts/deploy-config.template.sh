#!/bin/bash
# Template for deployment configuration

# Droplet Configuration
export DROPLET1_IP="DROPLET1_IP_HERE"
export DROPLET1_PRIV_IP="DROPLET1_PRIV_IP_HERE"
export DROPLET1_USER="root"
export DROPLET2_IP="DROPLET2_IP_HERE"  
export DROPLET2_PRIV_IP="DROPLET2_PRIV_IP_HERE"
export DROPLET2_USER="root"
export DROPLET3_IP="DROPLET3_IP_HERE"
export DROPLET3_PRIV_IP="DROPLET3_PRIV_IP_HERE"
export DROPLET3_USER="root"

# SSH 
export SSH_KEY_PATH="~/.ssh/digitalocean_rsa"

# Database 
export POSTGRES_PASSWORD="POSTGRES_PASSWORD"
export POSTGRES_USER="stockfellow_user"

# Redis
export REDIS_PASSWORD="REDIS_PASSWORD"

# Keycloak
export KEYCLOAK_ADMIN_PASSWORD="KEYCLOAK_PASSWORD"

# API Keys
export PAYSTACK_API_KEY="PAYSTACK_KEY"

# Email 
export MFA_MAIL_PASSWORD="EMAIL_APP_PASSWORD"