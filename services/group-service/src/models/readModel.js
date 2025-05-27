const mongoose = require('mongoose');
const eventStore = require('../events/eventStore');

const groupSchema = new mongoose.Schema({
  groupId: { type: String, required: true, unique: true },
  adminId: String,
  name: String,
  minContribution: Number,
  maxMembers: Number,
  description: String,
  profileImage: String,
  visibility: String,
  contributionFrequency: String,
  contributionDate: Date,
  payoutFrequency: String,
  payoutDate: Date,
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
          minContribution: event.data.minContribution,
          maxMembers: event.data.maxMembers,
          description: event.data.description,
          profileImage: event.data.profileImage || null,
          visibility: event.data.visibility,
          contributionFrequency: event.data.contributionFrequency,
          contributionDate: event.data.contributionDate ? new Date(event.data.contributionDate) : null,
          payoutFrequency: event.data.payoutFrequency,
          payoutDate: event.data.payoutDate ? new Date(event.data.payoutDate) : null,
          memberIds: event.data.memberIds || []
        };
      } else if (event.eventType === 'MemberAdded') {
        if (groupData) {
          groupData.memberIds = [...new Set([...groupData.memberIds, event.data.userId])]; // Avoid duplicates
          groupData.maxMembers = groupData.memberIds.length > groupData.maxMembers ? groupData.memberIds.length : groupData.maxMembers; // Update maxMembers if needed
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