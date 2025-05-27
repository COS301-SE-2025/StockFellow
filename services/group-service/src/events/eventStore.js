const mongoose = require('mongoose');

const eventSchema = new mongoose.Schema({
  eventType: String,
  data: Object,
  timestamp: { type: Date, default: Date.now }
});

// Index for both userId and groupId to support user and group events
eventSchema.index({ 'data.userId': 1 });
eventSchema.index({ 'data.groupId': 1 });

const Event = mongoose.model('Event', eventSchema);

class EventStore {
  async appendEvent(eventType, data) {
    const event = new Event({ eventType, data });
    await event.save();
    return event;
  }

  async getEvents(id) {
    // Support both userId and groupId queries
    return await Event.find({
      $or: [
        { 'data.userId': id },
        { 'data.groupId': id }
      ]
    }).sort({ timestamp: 1 });
  }
}

module.exports = new EventStore();