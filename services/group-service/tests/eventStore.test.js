const mongoose = require('mongoose');
const EventStore = require('../src/events/eventStore');

jest.mock('mongoose');

describe('EventStore', () => {
  let mockModel;
  let mockInstance;

  beforeEach(() => {
    jest.clearAllMocks();
    mockInstance = { save: jest.fn() };
    mockModel = function () { return mockInstance; };
    mockModel.find = jest.fn();
    mongoose.model.mockReturnValue(mockModel);
  });

  it('should append an event', async () => {
    const event = { eventType: 'TestEvent', data: { id: '123' }, timestamp: expect.any(Date) };
    mockInstance.save.mockResolvedValue(event);

    const result = await EventStore.appendEvent('TestEvent', { id: '123' });
    expect(result).toEqual(event);
    expect(mockInstance.save).toHaveBeenCalled();
  });

  it('should get events by id', async () => {
    const events = [{ eventType: 'TestEvent', data: { groupId: '123' } }];
    mockModel.find.mockReturnValue({
      sort: jest.fn().mockResolvedValue(events)
    });

    const result = await EventStore.getEvents('123');
    expect(result).toEqual(events);
    expect(mockModel.find).toHaveBeenCalledWith({
      $or: [{ 'data.userId': '123' }, { 'data.groupId': '123' }]
    });
  });

  it('should handle errors in appendEvent', async () => {
    mockInstance.save.mockRejectedValue(new Error('DB error'));
    await expect(EventStore.appendEvent('TestEvent', { id: '123' })).rejects.toThrow('DB error');
  });

  it('should handle errors in getEvents', async () => {
    mockModel.find.mockReturnValue({
      sort: jest.fn().mockRejectedValue(new Error('DB error'))
    });
    await expect(EventStore.getEvents('123')).rejects.toThrow('DB error');
  });
});