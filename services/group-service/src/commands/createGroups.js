const eventStore = require('../events/eventStore');
const readModel = require('../models/readModel');
const logger = require('../utils/logger');

class CreateGroupCommand {
  async execute({ groupId, adminId, name, contributionAmount, contributionType, numberOfMembers, description, payoutAmount }) {
    // Additional validation (already partially handled in route)
    if (!['monthly', 'bi-weekly', 'weekly'].includes(contributionType)) {
      throw new Error('Invalid contribution type');
    }

    const event = await eventStore.appendEvent('GroupCreated', {
      groupId,
      adminId,
      name,
      contributionAmount,
      contributionType,
      numberOfMembers,
      description,
      payoutAmount
    });

    await readModel.rebuildState(groupId);
    logger.info(`Group ${groupId} created by admin ${adminId}`);

    return event;
  }
}

module.exports = new CreateGroupCommand();