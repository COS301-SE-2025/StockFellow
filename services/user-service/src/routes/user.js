const express = require('express');
const router = express.Router();
const RegisterUserCommand = require('../commands/registerUser');
const readModel = require('../models/readModel');

// Register a new user
router.post('/register', async (req, res) => {
  const { userId, name, email, saId, password } = req.body;
  try {
    await RegisterUserCommand.execute(userId, name, email, saId, password);
    res.status(201).json({ message: 'User registered successfully' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// Get user profile
router.get('/:userId', async (req, res) => {
  const user = await readModel.getUser(req.params.userId);
  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }
  res.json(user);
});

module.exports = router;