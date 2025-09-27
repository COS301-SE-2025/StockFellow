# Deployment Directory #
This directory holds all scripts and files related to simulating and preparing the local host environment for deployment

## How to run ## 
All these commands should be run from root, in wsl if on windows
- You may have to first run ```chmod +x {path to file}```
- If you still get errors to convert the line endings to unix type ```sed -i 's/\r$//' {path to file}```
To start and stop the services:
- ```./deployment/deploy.sh prod up```
- ```./deployment/deploy.sh prod down```
- ```./deployment/deploy.sh prod build```
  - This should only need to be run if you changes any Docker files or application.properties/application.yml files
- ```./deployment/deploy.sh prod clean```
  - This will remove volumes and orphans
For additional info check out the deply.sh file which is well commented

## Additional Info ##
This setup uses a single postgres database which is different that the previous multiple postgres service approach. FOr this reason you may need to run the following to setup the databases:
- ```./deployment/database/init-multiple-databases.sh```
Alternatively to ensure this step has been completed correct you can trun this command to check if the databases are setup
- ```docker exec -it postgres psql -U postgres -c "\l"```

## Files ##
- ```database/init-multiple-databases.sh```
  - Creates multiple postgres databases in a single postgres service. This aims to replicate the production enviroment where resource are constrianed
  - Makes use of ```$POSTGRES_MULTIPLE_DATABASES``` to determine which databases to create
- ```deploy.sh```
  - This file is used to run the docker-compose.prod.yml which simulates CPU and memoery constraints of the proposed deployment environment
- ```local-ci.sh```
  - This script is used to simulate the current ci.yml pipeline and should be run prior to merging or creating a PR to main
- ```monitoring-stack.yml```
  - For resource monitoring to test how services run in constrained environment
