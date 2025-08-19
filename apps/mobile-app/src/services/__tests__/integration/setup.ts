import testAuthService from './testAuthService';
import userService from '../../userService';

const TEST_USER = {
  username: process.env.TEST_USERNAME || 'ltshikila',
  password: process.env.TEST_PASSWORD || '0824Kobe'
};

// Global test state
declare global {
  var testAuthToken: string;
  var testUserId: string;
  var testLoginMethod: 'regular' | 'test';
  var setupComplete: boolean;
}

// Initialize global variables
if (!global.setupComplete) {
  global.setupComplete = false;
  global.testAuthToken = '';
  global.testUserId = '';
  global.testLoginMethod = 'test';
}

// Mock the actual authService for other services
jest.mock('../../authService', () => ({
  apiRequest: jest.fn().mockImplementation(async (url: string, options: any = {}) => {
    // Use our test auth service for API requests
    return testAuthService.apiRequest(url, options);
  }),
  getTokens: jest.fn().mockImplementation(async () => {
    return testAuthService.getTokens();
  }),
}));

beforeAll(async () => {
  // Only run setup once
  if (global.setupComplete) {
    return;
  }

  try {
    console.log('Setting up integration tests...');
    
    const result = await testAuthService.testLogin(TEST_USER.username, TEST_USER.password);
    global.testLoginMethod = 'test';

    if (!result.success) {
      throw new Error('Test login failed: ' + result.error);
    }

    // Get test user ID using the mocked authService
    const profile = await userService.getProfile();
    global.testUserId = profile.user.userId;

    // Store token if available
    const tokens = await testAuthService.getTokens();
    if (tokens.accessToken) {
      global.testAuthToken = tokens.accessToken;
    }

    console.log('Integration test setup completed successfully');
    global.setupComplete = true;

  } catch (error) {
    console.error('Integration test setup failed:', error);
    // Instead of process.exit, throw an error that Jest can handle
    throw new Error(`Integration test setup failed: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}, 30000);

afterAll(async () => {
  // Only run cleanup once
  if (!global.setupComplete) {
    return;
  }

  // Global cleanup
  try {
    await testAuthService.logout();
    global.setupComplete = false;
    console.log('Integration test cleanup completed');
  } catch (error) {
    console.warn('Cleanup error:', error);
  }
});

export {};