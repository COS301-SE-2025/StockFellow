const mongoose = require('mongoose');
const eventStore = require('../events/eventStore');

const userSchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true },
  name: String,
  email: String,
  saId: String,
  mobileNumber: String
});

const User = mongoose.model('User', userSchema);

class ReadModel {
  async rebuildState(userId) {
    const events = await eventStore.getEvents(userId);
    let userData = null;

    for (const event of events) {
      if (event.eventType === 'UserRegistered') {
        userData = {
          userId: event.data.userId,
          name: event.data.name,
          email: event.data.email,
          saId: event.data.saId,
          mobileNumber: event.data.mobileNumber
        };
      }
    }

    if (userData) {
      await User.findOneAndUpdate(
        { userId: userData.userId },
        userData,
        { upsert: true, new: true }
      );
    }
  }

  async getUser(userId) {
    return await User.findOne({ userId });
  }

  async getAllUsers() {
    return await User.find();
  }
}

module.exports = new ReadModel();