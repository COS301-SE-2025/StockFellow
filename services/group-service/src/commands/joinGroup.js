const eventStore = require('../events/eventStore');
const readModel = require('../models/readModel');
const logger = require('../utils/logger');

class JoinGroupCommand {
  async execute({ groupId, userId }) {
    // Check if group exists
    const group = await readModel.getGroup(groupId);
    if (!group) {
      throw new Error('Group not found');
    }

    // Check if user is already a member
    if (group.memberIds.includes(userId)) {
      throw new Error('User is already a member of this group');
    }

    const event = await eventStore.appendEvent('MemberAdded', {
      groupId,
      userId
    });

    await readModel.rebuildState(groupId);
    logger.info(`User ${userId} joined group ${groupId}`);

    return event;
  }
}

module.exports = new JoinGroupCommand();