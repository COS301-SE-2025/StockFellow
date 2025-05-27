const express = require('express');
const mongoose = require('mongoose');
const session = require('express-session');
const authRoutes = require('./src/routes/auth');
const userRoutes = require('./src/routes/user');
const syncRoutes = require('./src/routes/sync')
require('dotenv').config();

const app = express();
mongoose.connect(process.env.MONGODB_URI, { useNewUrlParser: true, useUnifiedTopology: true });
app.use(session({ secret: 'session-secret', resave: false, saveUninitialized: true, store: new session.MemoryStore(), cookie: { maxAge: 15 * 60 * 1000 } }));
app.use(express.json());
app.use('/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/sync', syncRoutes);

app.listen(process.env.PORT || 4000, () => console.log('User Service running on port 4000'));