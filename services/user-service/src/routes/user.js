const express = require('express');
const router = express.Router();
const jwtMiddleware = require('../config/jwt');
const readModel = require('../models/readModel');
const registerUserCommand = require('../commands/registerUser');

// GET /api/users - List service info
router.get('/', async (req, res) => {
  try {
    res.json({ 
      service: 'User Service',
      version: '1.0.0',
      endpoints: [
        'GET /api/users/profile - Get user profile (requires auth)',
        'POST /api/users/register - Register new user (requires Keycloak token)',
        'GET /api/users/:id - Get user by ID (requires auth)'
      ]
    });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

// GET /api/users/profile - Get current user's profile
router.get('/profile', jwtMiddleware, async (req, res) => {
  try {
    const user = await readModel.getUser(req.user.sub);
    
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    res.json(user);
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

// GET /api/users/:id - Get user by ID
router.get('/:id', jwtMiddleware, async (req, res) => {
  try {
    const user = await readModel.getUser(req.params.id);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    res.json(user);
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

// POST /api/users/register - Register new user
router.post('/register', jwtMiddleware, async (req, res) => {
  try {
    const { name, email, saId, mobileNumber } = req.body;

    if (!name || !email || !saId || !mobileNumber) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    const event = await registerUserCommand.execute(req.user, { name, email, saId, mobileNumber });
    
    res.status(201).json({ 
      message: 'User registered successfully',
      userId: req.user.sub,
      eventId: event._id
    });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(400).json({ error: error.message });
  }
});

module.exports = router;