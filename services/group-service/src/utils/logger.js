const winston = require('winston');

const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: 'group-service.log' })
  ]
});

module.exports = logger;
// ./services/group-service/src/utils/logger.js (example)
/*module.exports = {
  info: jest.fn(),
  error: jest.fn()
};*/