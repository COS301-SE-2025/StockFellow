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
  payoutAmount: Number
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
          contributionAmount: event.data.contributionAmount,
          contributionType: event.data.contributionType,
          numberOfMembers: event.data.numberOfMembers,
          description: event.data.description,
          payoutAmount: event.data.payoutAmount
        };
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
}

module.exports = new ReadModel();