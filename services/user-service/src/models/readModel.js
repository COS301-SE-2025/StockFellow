const eventStore = require('../events/eventStore');
const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true },
  name: { type: String, required: true },
  email: { type: String, required: true },
  saId: { type: String, required: true }, // Encrypted in production
  tier: { type: String, default: 'Tier 1' }
});

const User = mongoose.model('User', userSchema);

class UserReadModel {
  async rebuildState(userId) {
    const events = await eventStore.getEvents(userId);
    for (const event of events) {
      if (event.eventType === 'UserRegistered') {
        await User.findOneAndUpdate(
          { userId: event.data.userId },
          {
            userId: event.data.userId,
            name: event.data.name,
            email: event.data.email,
            saId: event.data.saId,
            tier: 'Tier 1'
          },
          { upsert: true }
        );
      }
    }
  }

  async getUser(userId) {
    return await User.findOne({ userId });
  }
}

module.exports = new UserReadModel();