const express = require('express');
const crypto = require('crypto');
const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(express.json());
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
  next();
});

// In-memory storage (replace with database for persistence)
const users = new Map();
const transactions = new Map();
const webhookConfigs = new Map();

// Utility functions
const generateId = () => crypto.randomUUID();
const generateAccountNumber = () => Math.floor(Math.random() * 9000000000) + 1000000000;

// Simulate random failures for testing
const shouldSimulateError = () => Math.random() < 0.1; // 10% chance

// Helper to send webhooks
const sendWebhook = async (event, data) => {
  const config = webhookConfigs.get('default');
  if (!config) return;

  const payload = {
    event,
    timestamp: new Date().toISOString(),
    ...data
  };

  // Simulate webhook delivery (in real implementation, use HTTP client)
  console.log(`📡 Webhook: ${event} -> ${config.url}`, payload);
  
  // Simulate async webhook delivery
  setTimeout(() => {
    console.log(`✅ Webhook delivered: ${event}`);
  }, 100);
};

// ============ USER & PAYMENT METHOD ENDPOINTS ============

// Create user with mock bank account
app.post('/users', (req, res) => {
  const { name, email } = req.body;
  
  if (!name || !email) {
    return res.status(400).json({ 
      error: 'Name and email are required' 
    });
  }

  const userId = generateId();
  const accountNumber = generateAccountNumber();
  
  const user = {
    id: userId,
    name,
    email,
    accountNumber,
    balance: 10000.00, // Starting balance
    paymentMethods: [],
    createdAt: new Date().toISOString()
  };

  users.set(userId, user);

  res.status(201).json({
    userId,
    accountNumber,
    balance: user.balance,
    message: 'User created successfully'
  });
});

// Add payment method (simulate verification)
app.post('/users/:userId/payment-methods', (req, res) => {
  const { userId } = req.params;
  const { type, details, forceInvalid } = req.body;

  const user = users.get(userId);
  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }

  // Simulate validation
  const isValid = !forceInvalid && Math.random() > 0.2; // 80% success rate unless forced
  
  if (!isValid) {
    return res.status(400).json({
      error: 'Payment method validation failed',
      code: 'INVALID_PAYMENT_METHOD',
      details: 'Account number invalid or bank account frozen'
    });
  }

  const methodId = generateId();
  const paymentMethod = {
    id: methodId,
    type: type || 'bank_account',
    details,
    verified: true,
    createdAt: new Date().toISOString()
  };

  user.paymentMethods.push(paymentMethod);

  res.status(201).json({
    methodId,
    status: 'verified',
    message: 'Payment method added successfully'
  });
});

// Get user payment methods
app.get('/users/:userId/payment-methods', (req, res) => {
  const { userId } = req.params;
  const user = users.get(userId);
  
  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }

  res.json({
    paymentMethods: user.paymentMethods
  });
});

// Get user balance
app.get('/users/:userId/balance', (req, res) => {
  const { userId } = req.params;
  const user = users.get(userId);
  
  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }

  res.json({
    userId,
    balance: user.balance,
    accountNumber: user.accountNumber
  });
});

// ============ TRANSACTION ENDPOINTS ============

// Create transaction
app.post('/transactions', (req, res) => {
  const { fromUserId, toUserId, amount, description, mandateReference } = req.body;

  if (!fromUserId || !toUserId || !amount) {
    return res.status(400).json({
      error: 'fromUserId, toUserId, and amount are required'
    });
  }

  const fromUser = users.get(fromUserId);
  const toUser = users.get(toUserId);

  if (!fromUser || !toUser) {
    return res.status(404).json({ error: 'One or both users not found' });
  }

  const transactionId = generateId();
  
  // Simulate various error conditions
  if (shouldSimulateError()) {
    const errors = [
      { code: 'NETWORK_ERROR', message: 'Network timeout occurred' },
      { code: 'BANK_MAINTENANCE', message: 'Bank systems under maintenance' },
      { code: 'ACCOUNT_FROZEN', message: 'Source account is frozen' }
    ];
    
    const error = errors[Math.floor(Math.random() * errors.length)];
    
    const transaction = {
      id: transactionId,
      fromUserId,
      toUserId,
      amount,
      description,
      mandateReference,
      status: 'failed',
      error: error.code,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    transactions.set(transactionId, transaction);

    // Send webhook
    sendWebhook('transaction.failed', {
      transactionId,
      fromUserId,
      toUserId,
      amount,
      error: error.code,
      message: error.message
    });

    return res.status(400).json({
      transactionId,
      status: 'failed',
      error: error.code,
      message: error.message
    });
  }

  // Check insufficient funds
  if (fromUser.balance < amount) {
    const transaction = {
      id: transactionId,
      fromUserId,
      toUserId,
      amount,
      description,
      mandateReference,
      status: 'failed',
      error: 'INSUFFICIENT_FUNDS',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    transactions.set(transactionId, transaction);

    sendWebhook('transaction.failed', {
      transactionId,
      fromUserId,
      toUserId,
      amount,
      error: 'INSUFFICIENT_FUNDS',
      message: 'Insufficient funds in source account'
    });

    return res.status(400).json({
      transactionId,
      status: 'failed',
      error: 'INSUFFICIENT_FUNDS',
      message: 'Insufficient funds in source account'
    });
  }

  // Create successful transaction
  const transaction = {
    id: transactionId,
    fromUserId,
    toUserId,
    amount,
    description,
    mandateReference,
    status: 'processing',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  transactions.set(transactionId, transaction);

  // Simulate processing delay
  setTimeout(() => {
    // Update balances
    fromUser.balance -= amount;
    toUser.balance += amount;
    
    // Update transaction status
    transaction.status = 'completed';
    transaction.updatedAt = new Date().toISOString();
    
    // Send webhook
    sendWebhook('transaction.completed', {
      transactionId,
      fromUserId,
      toUserId,
      amount,
      description
    });

    console.log(`💸 Transaction completed: ${fromUserId} -> ${toUserId} (${amount})`);
  }, Math.random() * 3000 + 1000); // 1-4 second delay

  res.status(201).json({
    transactionId,
    status: 'processing',
    message: 'Transaction initiated successfully'
  });
});

// Get transaction details
app.get('/transactions/:transactionId', (req, res) => {
  const { transactionId } = req.params;
  const transaction = transactions.get(transactionId);

  if (!transaction) {
    return res.status(404).json({ error: 'Transaction not found' });
  }

  res.json(transaction);
});

// Get user transaction history
app.get('/users/:userId/transactions', (req, res) => {
  const { userId } = req.params;
  const userTransactions = Array.from(transactions.values())
    .filter(t => t.fromUserId === userId || t.toUserId === userId)
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

  res.json({
    transactions: userTransactions
  });
});

// ============ SIMULATION & TESTING ENDPOINTS ============

// Simulate scenarios for testing
app.post('/users/:userId/simulate-scenario', (req, res) => {
  const { userId } = req.params;
  const { scenario } = req.body;

  const user = users.get(userId);
  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }

  switch (scenario) {
    case 'insufficient_funds':
      user.balance = 0;
      break;
    case 'restore_balance':
      user.balance = 10000;
      break;
    case 'freeze_account':
      user.frozen = true;
      break;
    case 'unfreeze_account':
      user.frozen = false;
      break;
    default:
      return res.status(400).json({ error: 'Unknown scenario' });
  }

  res.json({
    message: `Scenario '${scenario}' applied to user ${userId}`,
    newBalance: user.balance,
    frozen: user.frozen || false
  });
});

// ============ WEBHOOK ENDPOINTS ============

// Configure webhooks
app.post('/webhooks/configure', (req, res) => {
  const { url, events } = req.body;

  if (!url) {
    return res.status(400).json({ error: 'Webhook URL is required' });
  }

  webhookConfigs.set('default', { url, events: events || [] });

  res.json({
    message: 'Webhook configured successfully',
    url,
    events
  });
});

// Test webhook
app.post('/webhooks/test', (req, res) => {
  sendWebhook('test.event', {
    message: 'This is a test webhook',
    timestamp: new Date().toISOString()
  });

  res.json({ message: 'Test webhook sent' });
});

// ============ ADMIN ENDPOINTS ============

// Get all transactions (admin)
app.get('/admin/transactions', (req, res) => {
  const allTransactions = Array.from(transactions.values())
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

  res.json({
    transactions: allTransactions,
    count: allTransactions.length
  });
});

// Get all users (admin)
app.get('/admin/users', (req, res) => {
  const allUsers = Array.from(users.values()).map(user => ({
    id: user.id,
    name: user.name,
    email: user.email,
    balance: user.balance,
    accountNumber: user.accountNumber,
    paymentMethodsCount: user.paymentMethods.length
  }));

  res.json({
    users: allUsers,
    count: allUsers.length
  });
});

// Reset system (for testing)
app.post('/system/reset', (req, res) => {
  users.clear();
  transactions.clear();
  webhookConfigs.clear();

  res.json({
    message: 'System reset successfully',
    timestamp: new Date().toISOString()
  });
});

// Health check
app.get('/system/health', (req, res) => {
  res.json({
    status: 'healthy',
    uptime: process.uptime(),
    timestamp: new Date().toISOString(),
    users: users.size,
    transactions: transactions.size
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({
    error: 'Internal server error',
    message: err.message
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    error: 'Endpoint not found',
    path: req.path
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 Mock Payment Service running on port ${PORT}`);
  console.log(`📋 Health check: http://localhost:${PORT}/system/health`);
  console.log(`🔧 Admin panel: http://localhost:${PORT}/admin/users`);
});

module.exports = app;