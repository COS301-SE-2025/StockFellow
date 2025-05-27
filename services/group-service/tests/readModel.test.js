const mongoose = require('mongoose');
const ReadModel = require('../src/models/readModel');
const eventStore = require('../src/events/eventStore');

jest.mock('../src/events/eventStore');
jest.mock('mongoose');

describe('ReadModel', () => {
  let mockModel;

  beforeEach(() => {
    jest.clearAllMocks();
    mockModel = {
      findOne: jest.fn(),
      find: jest.fn(),
      findOneAndUpdate: jest.fn()
    };
    mongoose.model.mockReturnValue(mockModel);
  });

  it('should rebuild state with GroupCreated event', async () => {
    const groupData = {
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
    eventStore.getEvents.mockResolvedValue([{ eventType: 'GroupCreated', data: groupData }]);
    mockModel.findOneAndUpdate.mockResolvedValue(groupData);

    await ReadModel.rebuildState('group_123');
    expect(eventStore.getEvents).toHaveBeenCalledWith('group_123');
    expect(mockModel.findOneAndUpdate).toHaveBeenCalledWith(
      { groupId: 'group_123' },
      expect.objectContaining(groupData),
      { upsert: true, new: true }
    );
  });

  it('should rebuild state with MemberAdded event', async () => {
    const groupData = {
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
    eventStore.getEvents.mockResolvedValue([
      { eventType: 'GroupCreated', data: groupData },
      { eventType: 'MemberAdded', data: { groupId: 'group_123', userId: 'userId2' } }
    ]);
    mockModel.findOneAndUpdate.mockResolvedValue(groupData);

    await ReadModel.rebuildState('group_123');
    expect(mockModel.findOneAndUpdate).toHaveBeenCalledWith(
      { groupId: 'group_123' },
      expect.objectContaining({
        memberIds: ['userId1', 'userId2'],
        maxMembers: 5
      }),
      { upsert: true, new: true }
    );
  });

  it('should handle no events', async () => {
    eventStore.getEvents.mockResolvedValue([]);
    await ReadModel.rebuildState('group_123');
    expect(mockModel.findOneAndUpdate).not.toHaveBeenCalled();
  });

  it('should get group by groupId', async () => {
    const group = { groupId: 'group_123' };
    mockModel.findOne.mockResolvedValue(group);

    const result = await ReadModel.getGroup('group_123');
    expect(result).toEqual(group);
    expect(mockModel.findOne).toHaveBeenCalledWith({ groupId: 'group_123' });
  });

  it('should get all groups', async () => {
    const groups = [{ groupId: 'group_123' }];
    mockModel.find.mockResolvedValue(groups);

    const result = await ReadModel.getAllGroups();
    expect(result).toEqual(groups);
    expect(mockModel.find).toHaveBeenCalled();
  });

  it('should get user groups', async () => {
    const groups = [{ groupId: 'group_123', memberIds: ['userId1'] }];
    mockModel.find.mockResolvedValue(groups);

    const result = await ReadModel.getUserGroups('userId1');
    expect(result).toEqual(groups);
    expect(mockModel.find).toHaveBeenCalledWith({ memberIds: 'userId1' });
  });

  it('should handle errors in rebuildState', async () => {
    eventStore.getEvents.mockRejectedValue(new Error('DB error'));
    await expect(ReadModel.rebuildState('group_123')).rejects.toThrow('DB error');
  });

  it('should handle errors in getGroup', async () => {
    mockModel.findOne.mockRejectedValue(new Error('DB error'));
    await expect(ReadModel.getGroup('group_123')).rejects.toThrow('DB error');
  });

  it('should handle errors in getAllGroups', async () => {
    mockModel.find.mockRejectedValue(new Error('DB error'));
    await expect(ReadModel.getAllGroups()).rejects.toThrow('DB error');
  });

  it('should handle errors in getUserGroups', async () => {
    mockModel.find.mockRejectedValue(new Error('DB error'));
    await expect(ReadModel.getUserGroups('userId1')).rejects.toThrow('DB error');
  });
});