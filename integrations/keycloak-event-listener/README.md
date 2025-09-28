# Keycloak Event Listener Extension
This extension enable listening for user registration and update event from keycloak which are used to sync the user-service db and keycloak user db

## How to run
* `cd keycloak-event-listener`
* `mvn clean compile` to test if everything is setup correctly
* `mvn clean package` to generate jar file
* Move jar file from this directory to ./integrations/keycloak-extension

## In keycloak
* Add event listener to realm