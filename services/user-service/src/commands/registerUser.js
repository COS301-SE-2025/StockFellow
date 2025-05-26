const eventStore = require('../events/eventStore');
const readModel = require('../models/readModel');
const logger = require('../utils/logger');

class RegisterUserCommand {
  async execute(token, userData) {
    const { name, email, saId, mobileNumber } = userData;

    if (!saId || saId.length !== 13 || !/^\d+$/.test(saId)) {
      throw new Error('Invalid SA ID');
    }
    if (!mobileNumber || !/^\+?\d{10,15}$/.test(mobileNumber)) {
      throw new Error('Invalid mobile number');
    }
    if (!name || !email) {
      throw new Error('Missing required fields');
    }

    // Assume token is verified by middleware; extract sub
    //const userId = token.sub;
    const userId = 'e20f93e2-d283-4100-a5fa-92c61d85b4f4'; // Placeholder for user ID, replace with actual user ID from JWT

    // Store user data in MongoDB via event sourcing
    const event = await eventStore.appendEvent('UserRegistered', {
      userId,
      name,
      email,
      saId,
      mobileNumber
    });
    await readModel.rebuildState(userId);
    logger.info(`User ${userId} registered successfully`);

    return event;
  }
}

module.exports = new RegisterUserCommand();