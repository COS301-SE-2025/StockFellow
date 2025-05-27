// const jwt = require('jsonwebtoken');
// const axios = require('axios');

// module.exports = async (req, res, next) => {
//   const token = req.headers.authorization?.split(' ')[1];
//   if (!token) {
//     return res.status(401).json({ error: 'No token provided' });
//   }

//   try {
//     const publicKey = await fetchPublicKey();
//     console.log(Public-Key : ${publicKey});
//     const decoded = jwt.verify(token, publicKey);
//     req.user = decoded; // Attach user info (e.g., sub, email)
//     next();
//   } catch (error) {
//     res.status(401).json({ error: 'Invalid or expired token' });
//   }
// };

// async function fetchPublicKey() {
//   const response = await axios.get(${process.env.KEYCLOAK_REALM_URL}/protocol/openid-connect/certs);
//   const cert = response.data.keys.find(key => key.use === 'sig').x5c[0];
//   return -----BEGIN CERTIFICATE-----\n${cert}\n-----END CERTIFICATE-----;
// }

const jwt = require('jsonwebtoken');
const jwksClient = require('jwks-rsa');

const client = jwksClient({
  jwksUri: process.env.KEYCLOAK_JWKS_URI || 'http://localhost:8080/realms/stockfellow/protocol/openid-connect/certs'
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
    issuer: 'http://localhost:8080/realms/stockfellow',
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

//Debug version

// const jwt = require('jsonwebtoken');
// const jwksClient = require('jwks-rsa');

// // Debug logging function
// const debugLog = (message, data = null) => {
//   console.log(`[JWT DEBUG] ${message}`);
//   if (data) {
//     console.log('[JWT DEBUG] Data:', JSON.stringify(data, null, 2));
//   }
// };

// // Log environment variables on startup
// debugLog('Environment Variables:', {
//   KEYCLOAK_JWKS_URI: process.env.KEYCLOAK_JWKS_URI,
//   KEYCLOAK_ISSUER: process.env.KEYCLOAK_ISSUER,
//   KEYCLOAK_REALM_URL: process.env.KEYCLOAK_REALM_URL
// });

// const jwksUri = process.env.KEYCLOAK_JWKS_URI || 'http://localhost:8080/realms/stockfellow/protocol/openid-connect/certs';


// // const expectedIssuer = process.env.KEYCLOAK_ISSUER || 'http://localhost:8080/realms/stockfellow';  
// // Currently cause issues due to token being receivd from localhost but used in docker
// //Temp fix below
// const expectedIssuer = 'http://localhost:8080/realms/stockfellow';



// debugLog('Using JWKS URI:', jwksUri);
// debugLog('Expected Issuer:', expectedIssuer);

// const client = jwksClient({
//   jwksUri: jwksUri,
//   requestHeaders: {}, // Optional: add headers if needed
//   timeout: 30000, // 30 second timeout
// });

// function getKey(header, callback) {
//   debugLog('Getting signing key for kid:', header.kid);
  
//   client.getSigningKey(header.kid, (err, key) => {
//     if (err) {
//       debugLog('Error getting signing key:', err.message);
//       return callback(err);
//     }
    
//     const signingKey = key?.publicKey || key?.rsaPublicKey;
//     debugLog('Successfully retrieved signing key');
//     callback(null, signingKey);
//   });
// }

// const jwtMiddleware = (req, res, next) => {
//   debugLog('=== JWT Middleware Started ===');
  
//   const authHeader = req.headers.authorization;
//   debugLog('Authorization header:', authHeader ? 'Present' : 'Missing');
  
//   if (!authHeader || !authHeader.startsWith('Bearer ')) {
//     debugLog('Invalid or missing Bearer token');
//     return res.status(401).json({ error: 'Missing or invalid Authorization header' });
//   }

//   const token = authHeader.split(' ')[1];
//   debugLog('Token length:', token.length);
  
//   // Decode token without verification to inspect claims
//   try {
//     const decoded = jwt.decode(token, { complete: true });
//     debugLog('Token header:', decoded.header);
//     debugLog('Token payload (selected fields):', {
//       iss: decoded.payload.iss,
//       aud: decoded.payload.aud,
//       exp: decoded.payload.exp,
//       exp_readable: new Date(decoded.payload.exp * 1000).toISOString(),
//       iat: decoded.payload.iat,
//       iat_readable: new Date(decoded.payload.iat * 1000).toISOString(),
//       sub: decoded.payload.sub,
//       typ: decoded.payload.typ
//     });
    
//     // Check if token is expired
//     const now = Math.floor(Date.now() / 1000);
//     if (decoded.payload.exp < now) {
//       debugLog('Token is EXPIRED!', {
//         exp: decoded.payload.exp,
//         now: now,
//         expired_seconds_ago: now - decoded.payload.exp
//       });
//     } else {
//       debugLog('Token expiry is valid', {
//         expires_in_seconds: decoded.payload.exp - now
//       });
//     }
    
//     // Check issuer match
//     if (decoded.payload.iss !== expectedIssuer) {
//       debugLog('ISSUER MISMATCH!', {
//         token_issuer: decoded.payload.iss,
//         expected_issuer: expectedIssuer
//       });
//     } else {
//       debugLog('Issuer matches expected value');
//     }
    
//   } catch (decodeError) {
//     debugLog('Error decoding token for inspection:', decodeError.message);
//   }

//   // Now verify the token
//   jwt.verify(token, getKey, {
//     issuer: expectedIssuer,
//     algorithms: ['RS256'],
//     clockTolerance: 60 // Allow 60 seconds clock skew
//   }, (err, decoded) => {
//     if (err) {
//       debugLog('JWT Verification FAILED:', {
//         name: err.name,
//         message: err.message,
//         expiredAt: err.expiredAt,
//         date: err.date
//       });
      
//       // Specific error handling
//       if (err.name === 'TokenExpiredError') {
//         return res.status(401).json({ 
//           error: 'Token expired', 
//           expiredAt: err.expiredAt,
//           debug: 'Token has expired. Please get a new token.'
//         });
//       } else if (err.name === 'JsonWebTokenError') {
//         return res.status(401).json({ 
//           error: 'Invalid token format', 
//           debug: err.message 
//         });
//       } else if (err.name === 'NotBeforeError') {
//         return res.status(401).json({ 
//           error: 'Token not active yet', 
//           debug: err.message 
//         });
//       } else {
//         return res.status(401).json({ 
//           error: 'Token verification failed', 
//           debug: err.message 
//         });
//       }
//     }
    
//     debugLog('JWT Verification SUCCESS!');
//     debugLog('Decoded user info:', {
//       sub: decoded.sub,
//       preferred_username: decoded.preferred_username,
//       email: decoded.email,
//       realm_access: decoded.realm_access
//     });
    
//     req.user = decoded;
//     debugLog('=== JWT Middleware Completed Successfully ===');
//     next();
//   });
// };

// module.exports = jwtMiddleware;