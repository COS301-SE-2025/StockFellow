const mongoose = require('mongoose');

const eventSchema = new mongoose.Schema({
  eventType: { type: String, required: true },
  data: { type: Object, required: true },
  timestamp: { type: Date, default: Date.now }
});

const Event = mongoose.model('Event', eventSchema);

class EventStore {
  async appendEvent(eventType, data) {
    const event = new Event({ eventType, data });
    await event.save();
    return event;
  }

  async getEvents(userId) {
    return await Event.find(userId ? { 'data.userId': userId } : {}).sort({ timestamp: 1 });
  }
}

module.exports = new EventStore();