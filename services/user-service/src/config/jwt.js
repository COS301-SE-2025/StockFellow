const jwt = require('jsonwebtoken');
const axios = require('axios');

module.exports = async (req, res, next) => {
  const token = req.headers.authorization?.split(' ')[1];
  console.log('Token received:', token ? 'YES' : 'NO');
  
  if (!token) {
    return res.status(401).json({ error: 'No token provided' });
  }

  try {
    console.log('Fetching public key from:', `${process.env.KEYCLOAK_REALM_URL}/protocol/openid-connect/certs`);
    const publicKey = await fetchPublicKey();
    console.log('Public key fetched successfully');
    
    const decoded = jwt.verify(token, publicKey);
    console.log('Token decoded successfully:', decoded.sub);
    req.user = decoded;
    next();
  } catch (error) {
    console.error('Token verification error:', error.message);
    res.status(401).json({ error: 'Invalid or expired token1' });
  }
};

async function fetchPublicKey() {
  try {
    const url = `${process.env.KEYCLOAK_REALM_URL}/protocol/openid-connect/certs`;
    console.log('Fetching from URL:', url);
    const response = await axios.get(url);
    const cert = response.data.keys.find(key => key.use === 'sig').x5c[0];
    return `-----BEGIN CERTIFICATE-----\n${cert}\n-----END CERTIFICATE-----`;
  } catch (error) {
    console.error('Failed to fetch public key:', error.message);
    throw error;
  }
}