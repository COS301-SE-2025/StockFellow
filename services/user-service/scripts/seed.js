const mongoose = require('mongoose');
const EventStore = require('../src/events/eventStore');
const UserReadModel = require('../src/models/readModel');
require('dotenv').config();

const mockUsers = [
  {
    userId: 'user1',
    name: 'John Doe',
    email: 'john.doe@example.com',
    saId: '1234567890123'
  },
  {
    userId: 'user2',
    name: 'Jane Smith',
    email: 'jane.smith@example.com',
    saId: '9876543210987'
  },
  {
    userId: 'user3',
    name: 'Thabo Mokoena',
    email: 'thabo.mokoena@example.com',
    saId: '4567891234567'
  }
];

async function seedDatabase() {
  try {
    // Connect to MongoDB
    await mongoose.connect(process.env.MONGODB_URI, {
      useNewUrlParser: true,
      useUnifiedTopology: true
    });
    console.log('Connected to MongoDB');

    // Clear existing data (optional, for clean seeding)
    await mongoose.connection.db.dropDatabase();
    console.log('Database cleared');

    // Insert mock events
    for (const user of mockUsers) {
      await EventStore.appendEvent('UserRegistered', {
        userId: user.userId,
        name: user.name,
        email: user.email,
        saId: user.saId
      });
      console.log(`Inserted UserRegistered event for ${user.userId}`);

      // Rebuild read model for each user
      await UserReadModel.rebuildState(user.userId);
      console.log(`Rebuilt read model for ${user.userId}`);
    }

    console.log('Mock data seeded successfully');
  } catch (error) {
    console.error('Error seeding database:', error.message);
  } finally {
    await mongoose.disconnect();
    console.log('Disconnected from MongoDB');
  }
}

seedDatabase();