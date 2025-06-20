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
    console.log("creating group.....");
    const { name, minContribution, maxMembers, description, profileImage, visibility, contributionFrequency, contributionDate, payoutFrequency, payoutDate, memberIds } = req.body;

    // Validate required fields
    if (!name || !minContribution || !maxMembers || !visibility || !contributionFrequency || !payoutFrequency) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    // Validate contributionFrequency
    if (!['Monthly', 'Bi-weekly', 'Weekly'].includes(contributionFrequency)) {
      return res.status(400).json({ error: 'Invalid contribution frequency. Valid values: Monthly, Bi-weekly, Weekly' });
    }

    // Validate payoutFrequency
    if (!['Monthly', 'Bi-weekly', 'Weekly'].includes(payoutFrequency)) {
      return res.status(400).json({ error: 'Invalid payout frequency. Valid values: Monthly, Bi-weekly, Weekly' });
    }

    // Validate visibility
    if (!['Private', 'Public'].includes(visibility)) {
      return res.status(400).json({ error: 'Invalid visibility' });
    }

    // Validate minContribution
    if (typeof minContribution !== 'number' || minContribution <= 0) {
      return res.status(400).json({ error: 'Invalid minimum contribution. Must be a positive number' });
    }

    // Validate maxMembers
    if (typeof maxMembers !== 'number' || maxMembers <= 0 || maxMembers > 30) {
      return res.status(400).json({ error: 'Invalid maximum members. Must be between 1-30' });
    }

    // Validate memberIds (optional, but must be an array of strings if provided)
    if (memberIds && (!Array.isArray(memberIds) || memberIds.some(id => typeof id !== 'string'))) {
      return res.status(400).json({ error: 'memberIds must be an array of strings' });
    }

    // Validate number of memberIds against maxMembers
    if (memberIds && memberIds.length > maxMembers) {
      return res.status(400).json({ error: 'Number of memberIds cannot exceed maxMembers' });
    }

    // Validate contributionDate and payoutDate (optional, but must be valid dates if provided)
    if (contributionDate && isNaN(new Date(contributionDate).getTime())) {
      return res.status(400).json({ error: 'Invalid contribution date' });
    }
    if (payoutDate && isNaN(new Date(payoutDate).getTime())) {
      return res.status(400).json({ error: 'Invalid payout date' });
    }

    const groupId = `group_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    //const adminId = req.user.sub;
    const adminId = 'e20f93e2-d283-4100-a5fa-92c61d85b4f4'; // Placeholder for admin ID, replace with actual user ID from JWT


    const event = await createGroupCommand.execute({
      groupId,
      adminId,
      name,
      minContribution,
      maxMembers,
      description,
      profileImage: profileImage || null,
      visibility,
      contributionFrequency,
      contributionDate: contributionDate ? new Date(contributionDate) : null,
      payoutFrequency,
      payoutDate: payoutDate ? new Date(payoutDate) : null,
      memberIds: memberIds || []
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
    const userId = '3372d535-05a1-4189-b6ff-a2291cb1145c'; // Placeholder for user ID, replace with actual user ID from JWT

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