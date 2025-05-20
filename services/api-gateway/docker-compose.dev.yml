# docker-compose.dev.yml
version: '3'

services:
  api-gateway:
    command: npx nodemon --inspect=0.0.0.0:9229 src/server.js
    ports:
      - "3000:3000"
      - "9229:9229"  # For Node.js debugging