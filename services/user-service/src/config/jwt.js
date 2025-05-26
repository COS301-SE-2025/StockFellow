// const jwt = require('jsonwebtoken');
// const axios = require('axios');

// module.exports = async (req, res, next) => {
//   const token = req.headers.authorization?.split(' ')[1];
//   if (!token) {
//     return res.status(401).json({ error: 'No token provided' });
//   }

//   try {
//     const publicKey = await fetchPublicKey();
//     console.log(`Public-Key : ${publicKey}`);
//     const decoded = jwt.verify(token, publicKey);
//     req.user = decoded; // Attach user info (e.g., sub, email)
//     next();
//   } catch (error) {
//     res.status(401).json({ error: 'Invalid or expired token' });
//   }
// };

// async function fetchPublicKey() {
//   const response = await axios.get(`${process.env.KEYCLOAK_REALM_URL}/protocol/openid-connect/certs`);
//   const cert = response.data.keys.find(key => key.use === 'sig').x5c[0];
//   return `-----BEGIN CERTIFICATE-----\n${cert}\n-----END CERTIFICATE-----`;
// }


const jwt = require('jsonwebtoken');
const jwksClient = require('jwks-rsa');

const client = jwksClient({
  jwksUri: process.env.KEYCLOAK_JWKS_URI || 'http://localhost:8080/auth/realms/stockfellow/protocol/openid-connect/certs'
});

function getKey(header, callback) {
  client.getSigningKey(header.kid, (err, key) => {
    const signingKey = key?.publicKey || key?.rsaPublicKey;
    callback(null, signingKey);
  });
}

const jwtMiddleware = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Missing or invalid Authorization header' });
  }

  const token = authHeader.split(' ')[1];
  jwt.verify(token, getKey, {
    issuer: process.env.KEYCLOAK_ISSUER || 'http://localhost:8080/auth/realms/stockfellow',
    algorithms: ['RS256']
  }, (err, decoded) => {
    if (err) {
      return res.status(401).json({ error: 'Invalid token' });
    }
    req.user = decoded;
    next();
  });
};

module.exports = jwtMiddleware;