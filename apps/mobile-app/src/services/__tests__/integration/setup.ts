import authService from '../../authService';
import userService from '../../userService';

const TEST_USER = {
  username: process.env.TEST_USERNAME || 'testuser@example.com',
  password: process.env.TEST_PASSWORD || 'TestPassword123!'
};

// Global test state
declare global {
  var testAuthToken: string;
  var testUserId: string;
  var testLoginMethod: 'regular' | 'test';
}

beforeAll(async () => {
  try {

    global.testLoginMethod = 'regular';
    const result = await authService.testLogin(TEST_USER.username, TEST_USER.password);
    global.testLoginMethod = 'test';

    if (!result.success) {
      throw new Error('Test login failed');
    }


    // Get test user ID
    const profile = await userService.getProfile();
    global.testUserId = profile.user.userId;

    // Store token if available
    const tokens = await authService.getTokens();
    if (tokens.accessToken) {
      global.testAuthToken = tokens.accessToken;
    }

  } catch (error) {
    console.error('Integration test setup failed:', error);
    process.exit(1);
  }
});

afterAll(async () => {
  // Global cleanup
  try {
    if (global.testAuthToken) {
      await authService.logout();
    }
  } catch (error) {
    console.warn('Cleanup error:', error);
  }
});

export { }; // This makes the file a module