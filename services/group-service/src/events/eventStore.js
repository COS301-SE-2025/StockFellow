const mongoose = require('mongoose');

const eventSchema = new mongoose.Schema({
  eventType: String,
  data: Object,
  timestamp: { type: Date, default: Date.now }
});
eventSchema.index({ 'data.groupId': 1 });
const Event = mongoose.model('GroupEvent', eventSchema);

class EventStore {
  async appendEvent(eventType, data) {
    const event = new Event({ eventType, data });
    await event.save();
    return event;
  }

  async getEvents(groupId) {
    return await Event.find({ 'data.groupId': groupId }).sort({ timestamp: 1 });
  }
}

module.exports = new EventStore();