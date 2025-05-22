const eventStore = require('../events/eventStore');
const readModel = require('../models/readModel');
const axios = require('axios');
const logger = require('../utils/logger');

class RegisterUserCommand {
  async execute(userId, name, email, saId, password) {
    if (!saId || saId.length !== 13 || !/^\d+$/.test(saId)) {
      throw new Error('Invalid SA ID');
    }

    try {
      await axios.post(
        'http://localhost:3000/auth/register', // Gateway endpoint
        { username: email, email, firstName: name.split(' ')[0], lastName: name.split(' ').slice(1).join(' '), enabled: true, credentials: [{ type: 'password', value: password, temporary: false }] },
        { headers: { 'Authorization': `Bearer mock-admin-token` } } // Mocked for Demo 1
      );
      logger.info(`User ${userId} registered via gateway`);
    } catch (error) {
      logger.error(`Registration failed: ${error.message}`);
      throw new Error('Registration failed');
    }

    const event = await eventStore.appendEvent('UserRegistered', { userId, name, email, saId });
    await readModel.rebuildState(userId);
    return event;
  }
}

module.exports = new RegisterUserCommand();