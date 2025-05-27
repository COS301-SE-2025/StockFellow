const request = require('supertest');
const express = require('express');
const createGroupCommand = require('../src/commands/createGroups');
const joinGroupCommand = require('../src/commands/joinGroup');
const readModel = require('../src/models/readModel');
const logger = require('../src/utils/logger');

const app = express();
app.use(express.json());
app.use('/api/groups', require('../src/routes/groups'));

describe('Group Service Routes', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('GET /api/groups', () => {
    it('should return service info', async () => {
      const response = await request(app).get('/api/groups');
      expect(response.status).toBe(200);
      expect(response.body).toEqual({
        service: 'Group Service',
        version: '1.0.0',
        endpoints: expect.any(Array)
      });
    });
  });

  describe('POST /api/groups/create', () => {
    beforeEach(() => {
      createGroupCommand.execute = jest.fn().mockResolvedValue({ _id: 'event_123' });
    });

    it('should create a group successfully', async () => {
      const response = await request(app)
        .post('/api/groups/create')
        .send({
          name: 'Test Group',
          minContribution: '100',
          maxMembers: '5',
          description: 'Test description',
          profileImage: 'image.jpg',
          visibility: 'Public',
          contributionFrequency: 'Monthly',
          contributionDate: '2025-06-01',
          payoutFrequency: 'Monthly',
          payoutDate: '2025-06-15',
          memberIds: ['userId1']
        });

      expect(response.status).toBe(201);
      expect(response.body).toEqual({
        message: 'Group created successfully',
        groupId: expect.any(String),
        eventId: 'event_123'
      });
      expect(createGroupCommand.execute).toHaveBeenCalledWith({
        groupId: expect.any(String),
        adminId: 'e20f93e2-d283-4100-a5fa-92c61d85b4f4',
        name: 'Test Group',
        minContribution: 100,
        maxMembers: 5,
        description: 'Test description',
        profileImage: 'image.jpg',
        visibility: 'Public',
        contributionFrequency: 'Monthly',
        contributionDate: expect.any(Date),
        payoutFrequency: 'Monthly',
        payoutDate: expect.any(Date),
        memberIds: ['userId1']
      });
    });

    it('should return 400 for missing required fields', async () => {
      const response = await request(app)
        .post('/api/groups/create')
        .send({ name: 'Test Group' });

      expect(response.status).toBe(400);
      expect(response.body).toEqual({ error: 'Missing required fields' });
    });

    it('should return 400 for invalid contribution frequency', async () => {
      const response = await request(app)
        .post('/api/groups/create')
        .send({
          name: 'Test Group',
          minContribution: '100',
          maxMembers: '5',
          visibility: 'Public',
          contributionFrequency: 'Invalid',
          payoutFrequency: 'Monthly'
        });

      expect(response.status).toBe(400);
      expect(response.body).toEqual({ error: 'Invalid contribution frequency' });
    });

    it('should return 400 for invalid minContribution', async () => {
      const response = await request(app)
        .post('/api/groups/create')
        .send({
          name: 'Test Group',
          minContribution: '-100',
          maxMembers: '5',
          visibility: 'Public',
          contributionFrequency: 'Monthly',
          payoutFrequency: 'Monthly'
        });

      expect(response.status).toBe(400);
      expect(response.body).toEqual({ error: 'Invalid minimum contribution' });
    });

    it('should return 400 for invalid maxMembers', async () => {
      const response = await request(app)
        .post('/api/groups/create')
        .send({
          name: 'Test Group',
          minContribution: '100',
          maxMembers: '0',
          visibility: 'Public',
          contributionFrequency: 'Monthly',
          payoutFrequency: 'Monthly'
        });

      expect(response.status).toBe(400);
      expect(response.body).toEqual({ error: 'Invalid maximum number of members' });
    });

    it('should return 400 for invalid memberIds', async () => {
      const response = await request(app)
        .post('/api/groups/create')
        .send({
          name: 'Test Group',
          minContribution: '100',
          maxMembers: '5',
          visibility: 'Public',
          contributionFrequency: 'Monthly',
          payoutFrequency: 'Monthly',
          memberIds: [123]
        });

      expect(response.status).toBe(400);
      expect(response.body).toEqual({ error: 'memberIds must be an array of strings' });
    });

    it('should return 400 for memberIds exceeding maxMembers', async () => {
      const response = await request(app)
        .post('/api/groups/create')
        .send({
          name: 'Test Group',
          minContribution: '100',
          maxMembers: '2',
          visibility: 'Public',
          contributionFrequency: 'Monthly',
          payoutFrequency: 'Monthly',
          memberIds: ['userId1', 'userId2', 'userId3']
        });

      expect(response.status).toBe(400);
      expect(response.body).toEqual({ error: 'Number of memberIds cannot exceed maxMembers' });
    });

    it('should return 400 for invalid contributionDate', async () => {
      const response = await request(app)
        .post('/api/groups/create')
        .send({
          name: 'Test Group',
          minContribution: '100',
          maxMembers: '5',
          visibility: 'Public',
          contributionFrequency: 'Monthly',
          payoutFrequency: 'Monthly',
          contributionDate: 'invalid-date'
        });

      expect(response.status).toBe(400);
      expect(response.body).toEqual({ error: 'Invalid contribution date' });
    });

    it('should handle errors from createGroupCommand', async () => {
      createGroupCommand.execute = jest.fn().mockRejectedValue(new Error('Command error'));
      const response = await request(app)
        .post('/api/groups/create')
        .send({
          name: 'Test Group',
          minContribution: '100',
          maxMembers: '5',
          visibility: 'Public',
          contributionFrequency: 'Monthly',
          payoutFrequency: 'Monthly'
        });

      expect(response.status).toBe(400);
      expect(response.body).toEqual({ error: 'Command error' });
    });
  });

  describe('GET /api/groups/user', () => {
    it('should return user groups', async () => {
      readModel.getUserGroups = jest.fn().mockResolvedValue([{ groupId: 'group_123' }]);
      const response = await request(app).get('/api/groups/user');
      expect(response.status).toBe(200);
      expect(response.body).toEqual([{ groupId: 'group_123' }]);
      expect(readModel.getUserGroups).toHaveBeenCalledWith('userId1');
    });

    it('should handle errors', async () => {
      readModel.getUserGroups = jest.fn().mockRejectedValue(new Error('DB error'));
      const response = await request(app).get('/api/groups/user');
      expect(response.status).toBe(500);
      expect(response.body).toEqual({ error: 'Internal server error' });
    });
  });

  describe('POST /api/groups/:groupId/join', () => {
    beforeEach(() => {
      readModel.getGroup = jest.fn();
      joinGroupCommand.execute = jest.fn();
    });

    it('should join a group successfully', async () => {
      readModel.getGroup.mockResolvedValue({ groupId: 'group_1748367693647_nmnukn659', memberIds: [] });
      joinGroupCommand.execute.mockResolvedValue({ _id: 'event_123' });
      const response = await request(app).post('/api/groups/group_1748367693647_nmnukn659/join');
      expect(response.status).toBe(200);
      expect(response.body).toEqual({
        message: 'Successfully joined group',
        groupId: 'group_1748367693647_nmnukn659',
        eventId: 'event_123'
      });
      expect(joinGroupCommand.execute).toHaveBeenCalledWith({
        groupId: 'group_1748367693647_nmnukn659',
        userId: '3372d535-05a1-4189-b6ff-a2291cb1145c'
      });
    });

    it('should return 404 if group not found', async () => {
      readModel.getGroup.mockResolvedValue(null);
      const response = await request(app).post('/api/groups/group_1748367693647_nmnukn659/join');
      expect(response.status).toBe(404);
      expect(response.body).toEqual({ error: 'Group not found' });
    });

    it('should return 400 if user is already a member', async () => {
      readModel.getGroup.mockResolvedValue({
        groupId: 'group_1748367693647_nmnukn659',
        memberIds: ['3372d535-05a1-4189-b6ff-a2291cb1145c']
      });
      const response = await request(app).post('/api/groups/group_1748367693647_nmnukn659/join');
      expect(response.status).toBe(400);
      expect(response.body).toEqual({ error: 'User is already a member of this group' });
    });

    it('should handle errors from joinGroupCommand', async () => {
      readModel.getGroup.mockResolvedValue({ groupId: 'group_1748367693647_nmnukn659', memberIds: [] });
      joinGroupCommand.execute.mockRejectedValue(new Error('Command error'));
      const response = await request(app).post('/api/groups/group_1748367693647_nmnukn659/join');
      expect(response.status).toBe(400);
      expect(response.body).toEqual({ error: 'Command error' });
    });
  });
});