const express = require('express');
const router = express.Router();
const jwtMiddleware = require('../config/jwt');
const readModel = require('../models/readModel');

router.get('/login', jwtMiddleware, async (req, res) => {
  try {
    const user = await readModel.getUser(req.user.sub);

    if (!user) {
      return res.status(404).json({ error: 'User not found in database' });
    }
    res.status(200).json({
      message: 'Login successful',
      user: {
        userId: req.user.sub,
        email: req.user.email,
        name: user.name,
        saId: user.saId,
        mobileNumber: user.mobileNumber
      }
    });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.get('/logout', (req, res) => {
  // Keycloak token invalidation is handled client-side; clear session if used
  if (req.session) {
    req.session.destroy();
  }
  res.json({ message: 'Logged out successfully' });
});

module.exports = router;