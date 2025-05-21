const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const EventStore = require('../src/events/eventStore');

let mongoServer;

beforeAll(async () => {
  mongoServer = await MongoMemoryServer.create();
  const uri = mongoServer.getUri();
  await mongoose.connect(uri, { useNewUrlParser: true, useUnifiedTopology: true });
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

describe('EventStore', () => {
  beforeEach(async () => {
    await mongoose.connection.db.dropDatabase();
  });

  it('should append an event to the store', async () => {
    const eventData = {
      userId: '123',
      name: 'John Doe',
      email: 'john@example.com',
      saId: '1234567890123'
    };
    const event = await EventStore.appendEvent('UserRegistered', eventData);

    expect(event.eventType).toBe('UserRegistered');
    expect(event.data).toEqual(eventData);
    expect(event.timestamp).toBeInstanceOf(Date);
  });

  it('should retrieve events for a user', async () => {
    const eventData = {
      userId: '123',
      name: 'John Doe',
      email: 'john@example.com',
      saId: '1234567890123'
    };
    await EventStore.appendEvent('UserRegistered', eventData);

    const events = await EventStore.getEvents('123');
    expect(events).toHaveLength(1);
    expect(events[0].eventType).toBe('UserRegistered');
    expect(events[0].data.userId).toBe('123');
  });

  it('should return empty array for non-existent user events', async () => {
    const events = await EventStore.getEvents('999');
    expect(events).toEqual([]);
  });
});