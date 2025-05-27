const eventStore = require('../events/eventStore');
const readModel = require('../models/readModel');
const logger = require('../utils/logger');

class CreateGroupCommand {
  async execute({ groupId, adminId, name, contributionAmount, contributionType, numberOfMembers, description, payoutAmount, memberIds }) {
    // Additional validation (already partially handled in route)
    if (!['monthly', 'bi-weekly', 'weekly'].includes(contributionType)) {
      throw new Error('Invalid contribution type');
    }

    // Validate memberIds
    if (!Array.isArray(memberIds) || memberIds.some(id => typeof id !== 'string')) {
      throw new Error('memberIds must be an array of strings');
    }

    // Validate numberOfMembers against memberIds (optional, depending on requirements)
    if (memberIds.length > numberOfMembers) {
      throw new Error('Number of memberIds cannot exceed numberOfMembers');
    }

    const event = await eventStore.appendEvent('GroupCreated', {
      groupId,
      adminId,
      name,
      contributionAmount,
      contributionType,
      numberOfMembers,
      description,
      payoutAmount,
      memberIds
    });

    await readModel.rebuildState(groupId);
    logger.info(`Group ${groupId} created by admin ${adminId}`);

    return event;
  }
}

module.exports = new CreateGroupCommand();