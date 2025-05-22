const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const UserReadModel = require('../../src/models/readModel');
const eventStore = require('../../src/events/eventStore');

jest.mock('../../src/events/eventStore');

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

describe('UserReadModel', () => {
  beforeEach(async () => {
    await mongoose.connection.db.dropDatabase();
    jest.clearAllMocks();
  });

  it('should rebuild state from UserRegistered event', async () => {
    const eventData = {
      userId: '123',
      name: 'John Doe',
      email: 'john@example.com',
      saId: '1234567890123'
    };
    eventStore.getEvents.mockResolvedValue([{ eventType: 'UserRegistered', data: eventData }]);

    await UserReadModel.rebuildState('123');
    const user = await UserReadModel.getUser('123');

    expect(user).toBeDefined();
    expect(user.userId).toBe('123');
    expect(user.name).toBe('John Doe');
    expect(user.email).toBe('john@example.com');
    expect(user.saId).toBe('1234567890123');
    expect(user.tier).toBe('Tier 1');
  });

  it('should return null for non-existent user', async () => {
    eventStore.getEvents.mockResolvedValue([]);
    await UserReadModel.rebuildState('999');
    const user = await UserReadModel.getUser('999');
    expect(user).toBeNull();
  });
});