const express = require('express');
const router = express.Router();
const jwtMiddleware = require('../config/jwt');

router.get('/login', jwtMiddleware, (req, res) => {
  res.json({ message: 'Login successful', user: { userId: req.user.sub, email: req.user.email } });
});

router.get('/logout', (req, res) => {
  req.session.destroy();
  res.json({ message: 'Logged out successfully' });
});

module.exports = router;