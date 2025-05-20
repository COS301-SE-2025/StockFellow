const express = require('express');
const mongoose = require('mongoose');
const Keycloak = require('keycloak-connect');
const session = require('express-session');
const authRoutes = require('./src/routes/auth');
const userRoutes = require('./src/routes/user');
require('dotenv').config();

const app = express();

// MongoDB Connection
mongoose.connect(process.env.MONGODB_URI, { useNewUrlParser: true, useUnifiedTopology: true })
  .then(() => console.log('Connected to MongoDB'))
  .catch(err => console.error('MongoDB connection error:', err));

// Session Setup for Keycloak
const memoryStore = new session.MemoryStore();
app.use(session({
  secret: 'session-secret',
  resave: false,
  saveUninitialized: true,
  store: memoryStore,
  cookie: { maxAge: 15 * 60 * 1000 } // 15 minutes inactivity timeout
}));

// Keycloak Setup
const keycloak = new Keycloak({ store: memoryStore }, {
  'realm': 'stokvel-realm',
  'auth-server-url': process.env.KEYCLOAK_REALM_URL,
  'clientId': process.env.KEYCLOAK_CLIENT_ID,
  'clientSecret': process.env.KEYCLOAK_CLIENT_SECRET,
  'ssl-required': 'external',
  'resource': process.env.KEYCLOAK_CLIENT_ID,
  'confidential-port': 0
});
app.use(keycloak.middleware());

// Middleware
app.use(express.json());

// Routes
app.use('/auth', authRoutes);
app.use('/api/users', keycloak.protect(), userRoutes);

// Start Server
const PORT = process.env.PORT || 3001;
app.listen(PORT, () => console.log(`User Service running on port ${PORT}`));