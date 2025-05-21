const mongoose = require('mongoose');

const eventSchema = new mongoose.Schema({
  eventType: String,
  data: Object,
  timestamp: { type: Date, default: Date.now }
});
eventSchema.index({ 'data.userId': 1 });
const Event = mongoose.model('Event', eventSchema);

class EventStore {
  async appendEvent(eventType, data) {
    const event = new Event({ eventType, data });
    await event.save();
    return event;
  }
  async getEvents(userId) {
    return await Event.find({ 'data.userId': userId }).sort({ timestamp: 1 });
  }
}
module.exports = new EventStore();