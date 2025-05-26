const express = require('express');
const router = express.Router();
const jwtMiddleware = require('../config/jwt');
const createGroupCommand = require('../commands/createGroup');

// GET /api/groups - Service info
router.get('/', async (req, res) => {
  try {
    res.json({
      service: 'Group Service',
      version: '1.0.0',
      endpoints: [
        'POST /api/groups/create - Create a new stokvel group (requires auth)',
        'GET /api/groups/:id - Get group details (requires auth)'
      ]
    });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

// POST /api/groups/create - Create a new stokvel group
router.post('/create', jwtMiddleware, async (req, res) => {
  try {
    const { name, contributionAmount, contributionType, numberOfMembers, description, payoutAmount } = req.body;

    if (!name || !contributionAmount || !contributionType || !numberOfMembers || !payoutAmount) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    if (!['monthly', 'bi-weekly', 'weekly'].includes(contributionType)) {
      return res.status(400).json({ error: 'Invalid contribution type' });
    }

    if (typeof contributionAmount !== 'number' || contributionAmount <= 0) {
      return res.status(400).json({ error: 'Invalid contribution amount' });
    }

    if (!Number.isInteger(numberOfMembers) || numberOfMembers <= 0) {
      return res.status(400).json({ error: 'Invalid number of members' });
    }

    if (typeof payoutAmount !== 'number' || payoutAmount <= 0) {
      return res.status(400).json({ error: 'Invalid payout amount' });
    }

    const groupId = `group_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const adminId = req.user.sub;

    const event = await createGroupCommand.execute({
      groupId,
      adminId,
      name,
      contributionAmount,
      contributionType,
      numberOfMembers,
      description,
      payoutAmount
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

module.exports = router;