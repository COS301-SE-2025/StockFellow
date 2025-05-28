const CreateGroupCommand = require('../src/commands/createGroups');
const eventStore = require('../src/events/eventStore');
const readModel = require('../src/models/readModel');
const logger = require('../src/utils/logger');

jest.mock('../src/events/eventStore');
jest.mock('../src/models/readModel');
jest.mock('../src/utils/logger');

describe('CreateGroupCommand', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should create a group successfully', async () => {
    eventStore.appendEvent.mockResolvedValue({ _id: 'event_123' });
    readModel.rebuildState.mockResolvedValue();
    const input = {
      groupId: 'group_123',
      adminId: 'admin_123',
      name: 'Test Group',
      minContribution: 100,
      maxMembers: 5,
      description: 'Test description',
      profileImage: 'image.jpg',
      visibility: 'Public',
      contributionFrequency: 'Monthly',
      contributionDate: new Date('2025-06-01'),
      payoutFrequency: 'Monthly',
      payoutDate: new Date('2025-06-15'),
      memberIds: ['userId1']
    };

    const result = await CreateGroupCommand.execute(input);
    expect(result).toEqual({ _id: 'event_123' });
    expect(eventStore.appendEvent).toHaveBeenCalledWith('GroupCreated', {
      ...input,
      contributionDate: expect.any(Date),
      payoutDate: expect.any(Date)
    });
    expect(readModel.rebuildState).toHaveBeenCalledWith('group_123');
    expect(logger.info).toHaveBeenCalledWith('Group group_123 created by admin admin_123');
  });

  it('should throw error for invalid contributionFrequency', async () => {
    const input = { contributionFrequency: 'Invalid', memberIds: [] };
    await expect(CreateGroupCommand.execute(input)).rejects.toThrow('Invalid contribution frequency');
  });

  it('should throw error for invalid payoutFrequency', async () => {
    const input = { contributionFrequency: 'Monthly', payoutFrequency: 'Invalid', memberIds: [] };
    await expect(CreateGroupCommand.execute(input)).rejects.toThrow('Invalid payout frequency');
  });

  it('should throw error for invalid visibility', async () => {
    const input = { contributionFrequency: 'Monthly', payoutFrequency: 'Monthly', visibility: 'Invalid', memberIds: [] };
    await expect(CreateGroupCommand.execute(input)).rejects.toThrow('Invalid visibility');
  });

  it('should throw error for invalid minContribution', async () => {
    const input = { contributionFrequency: 'Monthly', payoutFrequency: 'Monthly', visibility: 'Public', minContribution: -100, memberIds: [] };
    await expect(CreateGroupCommand.execute(input)).rejects.toThrow('Invalid minimum contribution');
  });

  it('should throw error for invalid maxMembers', async () => {
    const input = { contributionFrequency: 'Monthly', payoutFrequency: 'Monthly', visibility: 'Public', minContribution: 100, maxMembers: 0, memberIds: [] };
    await expect(CreateGroupCommand.execute(input)).rejects.toThrow('Invalid maximum number of members');
  });

  it('should throw error for invalid memberIds', async () => {
    const input = { contributionFrequency: 'Monthly', payoutFrequency: 'Monthly', visibility: 'Public', minContribution: 100, maxMembers: 5, memberIds: [123] };
    await expect(CreateGroupCommand.execute(input)).rejects.toThrow('memberIds must be an array of strings');
  });

  it('should throw error for memberIds exceeding maxMembers', async () => {
    const input = {
      contributionFrequency: 'Monthly',
      payoutFrequency: 'Monthly',
      visibility: 'Public',
      minContribution: 100,
      maxMembers: 2,
      memberIds: ['userId1', 'userId2', 'userId3']
    };
    await expect(CreateGroupCommand.execute(input)).rejects.toThrow('Number of memberIds cannot exceed maxMembers');
  });

  it('should throw error for invalid contributionDate', async () => {
    const input = {
      contributionFrequency: 'Monthly',
      payoutFrequency: 'Monthly',
      visibility: 'Public',
      minContribution: 100,
      maxMembers: 5,
      memberIds: [],
      contributionDate: 'invalid-date'
    };
    await expect(CreateGroupCommand.execute(input)).rejects.toThrow('Invalid contribution date');
  });

  it('should throw error for invalid payoutDate', async () => {
    const input = {
      contributionFrequency: 'Monthly',
      payoutFrequency: 'Monthly',
      visibility: 'Public',
      minContribution: 100,
      maxMembers: 5,
      memberIds: [],
      contributionDate: '2025-06-01',
      payoutDate: 'invalid-date'
    };
    await expect(CreateGroupCommand.execute(input)).rejects.toThrow('Invalid payout date');
  });
});