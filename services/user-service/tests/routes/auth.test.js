const request = require('supertest');
const express = require('express');
const session = require('express-session');
const jwt = require('jsonwebtoken');
const authRoutes = require('../../src/routes/auth');
const axios = require('axios');

jest.mock('axios');

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
  beforeEach(() => {
    jest.clearAllMocks();
    axios.get.mockResolvedValue({
      data: {
        keys: [{ use: 'sig', x5c: ['mock-cert'] }]
      }
    });
  });

  it('should handle successful login with valid JWT', async () => {
    const token = jwt.sign({ sub: 'user-123', email: 'john@example.com' }, 'mock-private-key');
    const response = await request(app)
      .get('/auth/login')
      .set('Authorization', `Bearer ${token}`);
    expect(response.status).toBe(200);
    expect(response.body).toEqual({
      message: 'Login successful',
      user: { userId: 'user-123', email: 'john@example.com' }
    });
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