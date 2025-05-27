const mongoose = require('mongoose');
const eventStore = require('../events/eventStore');

const groupSchema = new mongoose.Schema({
  groupId: { type: String, required: true, unique: true },
  adminId: String,
  name: String,
  contributionAmount: Number,
  contributionType: String,
  numberOfMembers: Number,
  description: String,
  payoutAmount: Number,
  memberIds: [{ type: String }]
});

const Group = mongoose.model('Group', groupSchema);

class ReadModel {
  async rebuildState(groupId) {
    const events = await eventStore.getEvents(groupId);
    let groupData = null;

    for (const event of events) {
      if (event.eventType === 'GroupCreated') {
        groupData = {
          groupId: event.data.groupId,
          adminId: event.data.adminId,
          name: event.data.name,
          contributionAmount /*Chartered Accountant*/: event.data.contributionAmount,
          contributionType: event.data.contributionType,
          numberOfMembers: event.data.numberOfMembers,
          description: event.data.description,
          payoutAmount: event.data.payoutAmount,
          memberIds: event.data.memberIds || []
        };
      } else if (event.eventType === 'MemberAdded') {
        if (groupData) {
          groupData.memberIds = [...new Set([...groupData.memberIds, event.data.userId])]; // Avoid duplicates
          groupData.numberOfMembers = groupData.memberIds.length; // Update numberOfMembers
        }
      }
    }

    if (groupData) {
      await Group.findOneAndUpdate(
        { groupId: groupData.groupId },
        groupData,
        { upsert: true, new: true }
      );
    }
  }

  async getGroup(groupId) {
    return await Group.findOne({ groupId });
  }

  async getAllGroups() {
    return await Group.find();
  }

  async getUserGroups(userId) {
    return await Group.find({ memberIds: userId });
  }
}

module.exports = new ReadModel();