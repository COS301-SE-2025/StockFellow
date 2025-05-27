const express = require('express');
const router = express.Router();
const jwtMiddleware = require('../config/jwt');
const createGroupCommand = require('../commands/createGroups');
const joinGroupCommand = require('../commands/joinGroup');
const readModel = require('../models/readModel');

// GET /api/groups - Service info
router.get('/', async (req, res) => {
  try {
    res.json({
      service: 'Group Service',
      version: '1.0.0',
      endpoints: [
        'POST /api/groups/create - Create a new stokvel group (requires auth)',
        'GET /api/groups/:id - Get group details (requires auth)',
        'GET /api/groups/user - Get groups for authenticated user (requires auth)',
        'POST /api/groups/:groupId/join - Join a group (requires auth)'
      ]
    });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

// POST /api/groups/create - Create a new stokvel group
router.post('/create', /*jwtMiddleware,*/ async (req, res) => {
  try {
    const { name, contributionAmount, contributionType, numberOfMembers, description, payoutAmount, memberIds } = req.body;

    // Validate required fields
    if (!name || !contributionAmount || !contributionType || !numberOfMembers || !payoutAmount) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    // Validate contributionType
    if (!['monthly', 'bi-weekly', 'weekly'].includes(contributionType)) {
      return res.status(400).json({ error: 'Invalid contribution type' });
    }

    // Validate contributionAmount
    if (typeof contributionAmount !== 'number' || contributionAmount <= 0) {
      return res.status(400).json({ error: 'Invalid contribution amount' });
    }

    // Validate numberOfMembers
    if (!Number.isInteger(numberOfMembers) || numberOfMembers <= 0) {
      return res.status(400).json({ error: 'Invalid number of members' });
    }

    // Validate payoutAmount
    if (typeof payoutAmount !== 'number' || payoutAmount <= 0) {
      return res.status(400).json({ error: 'Invalid payout amount' });
    }

    // Validate memberIds (optional, but must be an array of strings if provided)
    if (memberIds && (!Array.isArray(memberIds) || memberIds.some(id => typeof id !== 'string'))) {
      return res.status(400).json({ error: 'memberIds must be an array of strings' });
    }

    const groupId = `group_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    //const adminId = req.user.sub;
    const adminId = 'e20f93e2-d283-4100-a5fa-92c61d85b4f4'; // Placeholder for admin ID, replace with actual user ID from JWT

    const event = await createGroupCommand.execute({
      groupId,
      adminId,
      name,
      contributionAmount,
      contributionType,
      numberOfMembers,
      description,
      payoutAmount,
      memberIds: memberIds || [] // Default to empty array if not provided
    });

    res.status(201).json({
      message: 'Group created successfully',
      groupId: groupId,
      eventId: event._id
    });
  } catch (error) {
    console.error('Group creation error:', error);
    res.status(400).json({ error: error.message });
  }
});

// GET /api/groups/user - Get groups for authenticated user
router.get('/user', /*jwtMiddleware,*/ async (req, res) => {
  try {
    //const userId = req.user.sub;
    const userId = 'userId1';  //'e20f93e2-d283-4100-a5fa-92c61d85b4f4'; // Placeholder for user ID, replace with actual user ID from JWT
    const groups = await readModel.getUserGroups(userId);
    res.status(200).json(groups);
  } catch (error) {
    console.error('Error fetching user groups:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// POST /api/groups/:groupId/join - Join a group
router.post('/:groupId/join', /*jwtMiddleware,*/ async (req, res) => {
  try {
    //const groupId = req.params.groupId;
    const groupId = 'group_1748367693647_nmnukn659'; // Placeholder for group ID, replace with actual group ID from request params
   // const userId = req.user.sub;
   const userId ='3372d535-05a1-4189-b6ff-a2291cb1145c'; // Placeholder for user ID, replace with actual user ID from JWT

    // Check if group exists
    const group = await readModel.getGroup(groupId);
    if (!group) {
      return res.status(404).json({ error: 'Group not found' });
    }

    // Check if user is already a member
    if (group.memberIds.includes(userId)) {
      return res.status(400).json({ error: 'User is already a member of this group' });
    }

    const event = await joinGroupCommand.execute({ groupId, userId });
    res.status(200).json({
      message: 'Successfully joined group',
      groupId,
      eventId: event._id
    });
  } catch (error) {
    console.error('Error joining group:', error);
    res.status(400).json({ error: error.message });
  }
});


module.exports = router;