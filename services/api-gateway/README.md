# StockFellow API Gateway #
The API Gateway servers as the entry point to the systems microserves. It validates all requests sent to it and then performs: load balancing/rate limiting, authentiation and routing to the desired microservice. It provides a unified interface to the system for both the Web and Mobile application

## Features ##
* **Unified API Access**:
* **Authentication and Authorization**: (KeyCloak)
* **Request Routing**: Uses the fan out pattern to aggregate all resources required for the request
* **Rate Limiting**: Acts as gateway to protect backend services from excessive loads
* **Request Validation**: Ensures request data is valid before passing it on to backend services

## Technologies ##
* Node.js
* Keycloak integration (Exernal)
* Redis for rate limiting (Planned)
* Morgan for logging

## Files ##
* server.js is te main entry point which makes calls to 
  * middleware/ - these file act like functions which perform specific checks like calls to keycloak for autherization
  * routes/ - These files define the route to the required resource and then pass on the requests. One for each service
* package.json, README and Dockerfile allow us to build this service independently
  
## Keycloak ##
* Uses `keycloak-connect` which is an adapter that uses OpenID Connect protocol
* Uses docker to run so may need a keycloak.yml or it can be integrated into other workflows
* To run (FOR DEV PURPOSES ONLY) use:
  * `docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev`
  * Then access at http://localhost:8080/admin/

## Routes ##
* user
* transaction
* groups

## How to run ##
* Use command `docker-compose -f docker-compose.yml -f docker-compose.dev.yml up` to creating and start running server.js, keycloak and its postgres DB
* Enter `http://localhost:3000/user` into browser which should redirect to google
* `http://localhost:3000/transaction` and `http://localhost:3000/transaction` should show "Access Denied"

## Setting up Keycloak(For development) ##
* Running the docker-compose up command should import the realm-exports/stockfellow-realm.json file to set up the keycloak realm
* Realm shouls contain:
  * `public-client`
  * `confidential-client`
* You will need to create your own users for testing