const request = require('supertest');
const express = require('express');
const session = require('express-session');
const jwt = require('jsonwebtoken');
const axios = require('axios');

// Mock axios before any imports
jest.mock('axios');

// Mock environment variables
process.env.KEYCLOAK_REALM_URL = 'http://mock-keycloak-url';

const authRoutes = require('../../src/routes/auth');

const app = express();
app.use(express.json());
app.use(
  session({
    secret: 'session-secret',
    resave: false,
    saveUninitialized: true,
    store: new session.MemoryStore(),
    cookie: { maxAge: 15 * 60 * 1000 }
  })
);
app.use('/auth', authRoutes);

describe('Auth Routes', () => {
  // Create a test key pair for JWT signing/verification
  const testPrivateKey = `-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC5D6F9dqGQqJ7T
TestKeyForMockingPurposes123456789ABCDEF
-----END PRIVATE KEY-----`;

  const testPublicCert = `MIICmzCCAYMCBgGK1234567890TestCertForMockingPurposes`;

  beforeEach(() => {
    jest.clearAllMocks();
    
    // Mock the axios call to return a properly formatted certificate
    axios.get.mockResolvedValue({
      data: {
        keys: [{ 
          use: 'sig', 
          x5c: [testPublicCert]
        }]
      }
    });
  });

  it('should handle successful login with valid JWT', async () => {
    // Create a JWT token that will be verified with our mock setup
    const payload = { sub: 'user-123', email: 'john@example.com' };
    
    // Mock jwt.verify to return our expected payload
    const originalVerify = jwt.verify;
    jwt.verify = jest.fn().mockReturnValue(payload);

    const token = 'mock-jwt-token'; // We don't need a real token since we're mocking verification
    
    const response = await request(app)
      .get('/auth/login')
      .set('Authorization', `Bearer ${token}`);
    
    expect(response.status).toBe(200);
    expect(response.body).toEqual({
      message: 'Login successful',
      user: { userId: 'user-123', email: 'john@example.com' }
    });

    // Restore original jwt.verify
    jwt.verify = originalVerify;
  });

  it('should reject login with invalid token', async () => {
    // Mock jwt.verify to throw an error (invalid token)
    const originalVerify = jwt.verify;
    jwt.verify = jest.fn().mockImplementation(() => {
      throw new Error('invalid token');
    });

    const response = await request(app)
      .get('/auth/login')
      .set('Authorization', 'Bearer invalid-token');
    
    expect(response.status).toBe(401);
    expect(response.body).toEqual({ error: 'Invalid or expired token' });

    // Restore original jwt.verify
    jwt.verify = originalVerify;
  });

  it('should reject login with missing token', async () => {
    const response = await request(app).get('/auth/login');
    expect(response.status).toBe(401);
    expect(response.body).toEqual({ error: 'No token provided' });
  });

  it('should handle logout', async () => {
    const response = await request(app).get('/auth/logout');
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ message: 'Logged out successfully' });
  });
});