const express = require('express');
const router = express.Router();
const jwtMiddleware = require('../config/jwt');
const readModel = require('../models/readModel');

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

module.exports = router;