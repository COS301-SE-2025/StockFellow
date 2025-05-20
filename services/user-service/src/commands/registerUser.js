const eventStore = require('../events/eventStore');
const readModel = require('../models/readModel');
const axios = require('axios');
const logger = require('../utils/logger');
require('dotenv').config();

class RegisterUserCommand {
  async execute(userId, name, email, saId, password) {
    // Validate SA ID (mocked verification for Demo 1)
    if (!saId || saId.length !== 13 || !/^\d+$/.test(saId)) {
      throw new Error('Invalid SA ID');
    }

    // Register user in Keycloak
    try {
      const response = await axios.post(
        `${process.env.KEYCLOAK_REALM_URL}/protocol/openid-connect/registrations`,
        {
          username: email,
          email,
          firstName: name.split(' ')[0],
          lastName: name.split(' ').slice(1).join(' '),
          enabled: true,
          credentials: [{ type: 'password', value: password, temporary: false }]
        },
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': `Bearer ${await this.getAdminToken()}`
          }
        }
      );
      logger.info(`User ${userId} registered in Keycloak`);
    } catch (error) {
      logger.error(`Keycloak registration failed for user ${userId}: ${error.message}`);
      throw new Error('Keycloak registration failed');
    }

    // Emit UserRegistered event
    const event = await eventStore.appendEvent('UserRegistered', { userId, name, email, saId });
    
    // Update read model
    await readModel.rebuildState(userId);
    
    logger.info(`User ${userId} registered successfully`);
    return event;
  }

  async getAdminToken() {
    // Mocked admin token retrieval for Demo 1
    return 'mock-admin-token';
  }
}

module.exports = new RegisterUserCommand();