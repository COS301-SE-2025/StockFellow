#!/bin/bash
# deploy-all.sh - Deploy to all droplets

set -e

echo "Starting full deployment to all droplets..."

# Update environment variables with actual IPs
echo "Please ensure you've updated the IP addresses in all .env files!"

# Deploy in order (infrastructure first)
echo "Step 1: Deploying Infrastructure Services..."
./deploy-droplet1.sh

echo "Waiting 60 seconds for infrastructure services to be ready..."
sleep 60

echo "Step 2: Deploying Gateway Services..."  
./deploy-droplet2.sh

echo "Waiting 30 seconds for gateway services to be ready..."
sleep 30

echo "Step 3: Deploying Business Services..."
./deploy-droplet3.sh

echo "All deployments completed!"
echo "Please verify services are running by checking health endpoints."