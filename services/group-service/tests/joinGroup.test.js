const JoinGroupCommand = require('../src/commands/joinGroup');
const eventStore = require('../src/events/eventStore');
const readModel = require('../src/models/readModel');
const logger = require('../src/utils/logger');

jest.mock('../src/events/eventStore');
jest.mock('../src/models/readModel');
jest.mock('../src/utils/logger');

describe('JoinGroupCommand', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should join a group successfully', async () => {
    readModel.getGroup.mockResolvedValue({ groupId: 'group_123', memberIds: [] });
    eventStore.appendEvent.mockResolvedValue({ _id: 'event_123' });
    readModel.rebuildState.mockResolvedValue();

    const result = await JoinGroupCommand.execute({ groupId: 'group_123', userId: 'user_123' });
    expect(result).toEqual({ _id: 'event_123' });
    expect(readModel.getGroup).toHaveBeenCalledWith('group_123');
    expect(eventStore.appendEvent).toHaveBeenCalledWith('MemberAdded', { groupId: 'group_123', userId: 'user_123' });
    expect(readModel.rebuildState).toHaveBeenCalledWith('group_123');
    expect(logger.info).toHaveBeenCalledWith('User user_123 joined group group_123');
  });

  it('should throw error if group not found', async () => {
    readModel.getGroup.mockResolvedValue(null);
    await expect(JoinGroupCommand.execute({ groupId: 'group_123', userId: 'user_123' }))
      .rejects.toThrow('Group not found');
  });

  it('should throw error if user is already a member', async () => {
    readModel.getGroup.mockResolvedValue({ groupId: 'group_123', memberIds: ['user_123'] });
    await expect(JoinGroupCommand.execute({ groupId: 'group_123', userId: 'user_123' }))
      .rejects.toThrow('User is already a member of this group');
  });
});