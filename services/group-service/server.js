const express = require('express');
const mongoose = require('mongoose');
require('dotenv').config();
const groupRoutes = require('./src/routes/group');

const app = express();

mongoose.connect(process.env.MONGODB_URI || 'mongodb://mongodb:27017/stokvel_db', {
  useNewUrlParser: true,
  useUnifiedTopology: true
}).then(() => console.log('Connected to MongoDB'))
  .catch(err => console.error('MongoDB connection error:', err));

app.use(express.json());
app.use('/api/groups', groupRoutes);

const port = process.env.PORT || 4050;
app.listen(port, () => console.log(`Group Service running on port ${port}`));