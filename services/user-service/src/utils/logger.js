const winston = require('winston');

const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(), // For Demo 1, log to console
    // In production, add file transport for compliance
  ]
});

module.exports = logger;