version: '3'

services:
  postgres:
    image: postgres:15
    container_name: keycloak-postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "keycloak"]
      interval: 5s
      timeout: 5s
      retries: 5

  keycloak:
    image: quay.io/keycloak/keycloak:latest
    container_name: keycloak
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: password
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    ports:
      - "8080:8080"
    volumes:
      - ./realm-exports:/opt/keycloak/data/import
    command: start-dev --import-realm

  api-gateway:
    build: . 
    container_name: api-gateway
    depends_on:
      - keycloak
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=development
      # Enviroment variables for API
    volumes:
      - ./src:/usr/src/app/src
      - /usr/src/app/node_modules
    command: npx nodemon src/server.js

volumes:
  postgres_data: