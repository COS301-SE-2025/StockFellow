const RegisterUserCommand = require('../../src/commands/registerUser');
const eventStore = require('../../src/events/eventStore');
const readModel = require('../../src/models/readModel');
const nock = require('nock');
const logger = require('../../src/utils/logger');

jest.mock('../../src/events/eventStore');
jest.mock('../../src/models/readModel');
jest.mock('../../src/utils/logger');

describe('RegisterUserCommand', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should validate SA ID and register user via gateway', async () => {
    const userData = {
      userId: '123',
      name: 'John Doe',
      email: 'john@example.com',
      saId: '1234567890123',
      password: 'secure123'
    };

    nock('http://localhost:3000')
      .post('/auth/register')
      .reply(201, { id: 'keycloak-user-id' });

    eventStore.appendEvent.mockResolvedValue({ eventType: 'UserRegistered', data: userData });
    readModel.rebuildState.mockResolvedValue();

    const result = await RegisterUserCommand.execute(
      userData.userId,
      userData.name,
      userData.email,
      userData.saId,
      userData.password
    );

    expect(nock.isDone()).toBe(true);
    expect(eventStore.appendEvent).toHaveBeenCalledWith('UserRegistered', {
      userId: '123',
      name: 'John Doe',
      email: 'john@example.com',
      saId: '1234567890123'
    });
    expect(readModel.rebuildState).toHaveBeenCalledWith('123');
    expect(result).toEqual({ eventType: 'UserRegistered', data: userData });
    expect(logger.info).toHaveBeenCalledWith('User 123 registered via gateway');
  });

  it('should throw error for invalid SA ID (wrong length)', async () => {
    await expect(
      RegisterUserCommand.execute('123', 'John Doe', 'john@example.com', '123', 'secure123')
    ).rejects.toThrow('Invalid SA ID');
    expect(logger.error).not.toHaveBeenCalled();
  });

  it('should throw error for non-numeric SA ID', async () => {
    await expect(
      RegisterUserCommand.execute('123', 'John Doe', 'john@example.com', '123abc456def7', 'secure123')
    ).rejects.toThrow('Invalid SA ID');
    expect(logger.error).not.toHaveBeenCalled();
  });

  it('should handle gateway registration failure', async () => {
    nock('http://localhost:3000')
      .post('/auth/register')
      .reply(400, { error: 'Invalid request' });

    await expect(
      RegisterUserCommand.execute('123', 'John Doe', 'john@example.com', '1234567890123', 'secure123')
    ).rejects.toThrow('Registration failed');
    expect(logger.error).toHaveBeenCalledWith('Registration failed: Request failed with status code 400');
  });
});