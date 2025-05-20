const express = require('express');
const router = express.Router();
const keycloak = require('keycloak-connect');

// Redirect to Keycloak for login
router.get('/login', keycloak.protect(), (req, res) => {
  res.json({ message: 'Login successful', user: req.kauth.grant.access_token });
});

// Logout
router.get('/logout', keycloak.protect(), (req, res) => {
  req.session.destroy();
  res.json({ message: 'Logged out successfully' });
});

module.exports = router;