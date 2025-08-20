#!/bin/bash

# Read .env file and upload each variable as a secret
while IFS='=' read -r key value || [ -n "$key" ]; do
  # Skip comments and empty lines
  if [[ $key =~ ^#.*$ ]] || [[ -z "$key" ]]; then
    continue
  fi
  
  # Remove any quotes from value
  value=$(echo "$value" | sed 's/^["'\'']\|["'\'']$//g')
  
  echo "Adding secret: $key"
  gh secret set "$key" --body "$value"
done < .env.example
