const mongoose = require('mongoose');
const eventStore = require('../events/eventStore');

const userSchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true },
  username: { type: String, required: true },
  email: { type: String, required: true },
  firstName: String,
  lastName: String,
  emailVerified: { type: Boolean, default: false },
  contactNumber: String,
  idNumber: String,
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

const User = mongoose.model('User', userSchema);

class ReadModel {
  async rebuildState(userId) {
    const events = await eventStore.getEvents(userId);
    let userData = null;

    for (const event of events) {
      if (event.eventType === 'UserRegistered') {
        //Create new user
        userData = {
          userId: event.data.userId,
          username: event.data.username,
          email: event.data.email,
          firstName: event.data.firstName,
          lastName: event.data.lastName,
          emailVerified: event.data.emailVerified,
          contactNumber: event.data.contactNumber,
          idNumber: event.data.idNumber,
          createdAt: event.data.createdAt,
          updatedAt: event.data.updatedAt
        };
      } else if (event.eventType === 'UserUpdated') {
        // Update  user data
        if (userData) {
          userData = {
            ...userData,
            username: event.data.username !== undefined ? event.data.username : userData.username,
            email: event.data.email !== undefined ? event.data.email : userData.email,
            firstName: event.data.firstName !== undefined ? event.data.firstName : userData.firstName,
            lastName: event.data.lastName !== undefined ? event.data.lastName : userData.lastName,
            emailVerified: event.data.emailVerified !== undefined ? event.data.emailVerified : userData.emailVerified,
            contactNumber: event.data.contactNumber !== undefined ? event.data.contactNumber : userData.contactNumber,
            idNumber: event.data.idNumber !== undefined ? event.data.idNumber : userData.idNumber,
            updatedAt: event.data.updatedAt || event.timestamp
          };
        }
      }
    }

    if (userData) {
      await User.findOneAndUpdate(
        { userId: userData.userId },
        userData,
        { upsert: true, new: true }
      );
      console.log(`Read model updated for user: ${userData.userId}`);
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