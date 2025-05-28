const eventStore = require('../events/eventStore');
const readModel = require('../models/readModel');
const logger = require('../utils/logger');

class CreateGroupCommand {
  async execute({ groupId, adminId, name, minContribution, maxMembers, description, profileImage, visibility, contributionFrequency, contributionDate, payoutFrequency, payoutDate, memberIds }) {
    // Validate contributionFrequency
    if (!['Monthly', 'Bi-weekly', 'Weekly'].includes(contributionFrequency)) {
      throw new Error('Invalid contribution frequency');
    }

    // Validate payoutFrequency
    if (!['Monthly', 'Bi-weekly', 'Weekly'].includes(payoutFrequency)) {
      throw new Error('Invalid payout frequency');
    }

    // Validate visibility
    if (!['Private', 'Public'].includes(visibility)) {
      throw new Error('Invalid visibility');
    }

    // Validate minContribution
    if (typeof minContribution !== 'number' || minContribution <= 0) {
      throw new Error('Invalid minimum contribution');
    }

    // Validate maxMembers
    if (!Number.isInteger(maxMembers) || maxMembers <= 0) {
      throw new Error('Invalid maximum number of members');
    }

    // Validate memberIds
    if (!Array.isArray(memberIds) || memberIds.some(id => typeof id !== 'string')) {
      throw new Error('memberIds must be an array of strings');
    }

    // Validate number of memberIds against maxMembers
    if (memberIds.length > maxMembers) {
      throw new Error('Number of memberIds cannot exceed maxMembers');
    }

    // Validate contributionDate and payoutDate (optional, but must be valid dates if provided)
    if (contributionDate && isNaN(new Date(contributionDate).getTime())) {
      throw new Error('Invalid contribution date');
    }
    if (payoutDate && isNaN(new Date(payoutDate).getTime())) {
      throw new Error('Invalid payout date');
    }

    const event = await eventStore.appendEvent('GroupCreated', {
      groupId,
      adminId,
      name,
      minContribution,
      maxMembers,
      description,
      profileImage: profileImage || null,
      visibility,
      contributionFrequency,
      contributionDate: contributionDate ? new Date(contributionDate) : null,
      payoutFrequency,
      payoutDate: payoutDate ? new Date(payoutDate) : null,
      memberIds
    });

    await readModel.rebuildState(groupId);
    logger.info(`Group ${groupId} created by admin ${adminId}`);

    return event;
  }
}

module.exports = new CreateGroupCommand();