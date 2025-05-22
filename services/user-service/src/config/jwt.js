const jwt = require('jsonwebtoken');
const axios = require('axios');

module.exports = async (req, res, next) => {
  const token = req.headers.authorization?.split(' ')[1];
  if (!token) {
    return res.status(401).json({ error: 'No token provided' });
  }

  try {
    const publicKey = await fetchPublicKey();
    console.log(`Public-Key : ${publicKey}`);
    const decoded = jwt.verify(token, publicKey);
    req.user = decoded; // Attach user info (e.g., sub, email)
    next();
  } catch (error) {
    res.status(401).json({ error: 'Invalid or expired token' });
  }
};

async function fetchPublicKey() {
  const response = await axios.get(`${process.env.KEYCLOAK_REALM_URL}/protocol/openid-connect/certs`);
  const cert = response.data.keys.find(key => key.use === 'sig').x5c[0];
  return `-----BEGIN CERTIFICATE-----\n${cert}\n-----END CERTIFICATE-----`;
}