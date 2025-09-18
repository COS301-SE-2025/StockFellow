# Deployment Directory #
This directory holds all scripts and files related to simulating and preparing the local host environment for deployment

## Files ##
- ```database/init-multiple-databases.sh```
  - Creates multiple postgres databases in a single postgres service. This aims to replicate the production enviroment where resource are constrianed
  - Makes use of ```$POSTGRES_MULTIPLE_DATABASES``` to determine which databases to create
- ```monitoring/prometheus.yml```
  - For resource monitoring to test how services run in constrained environment
- ```deploy.sh```
  - This file is used to run the docker-compose.prod.yml which simulates CPU and memoery constraints of the proposed deployment environment
- ```local-ci.sh```
  - This script is used to simulate the current ci.yml pipeline and should be run prior to merging or creating a PR to main
- ```monitoring-stack.yml```
  - For resource monitoring to test how services run in constrained environment