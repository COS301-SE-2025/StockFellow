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
}

beforeAll(async () => {
  try {
    const result = await authService.login(TEST_USER.username, TEST_USER.password);
    
    if (!result.success) {
      throw new Error('Test user authentication failed');
    }
    
    if (result.mfaRequired) {
      throw new Error('Test user requires MFA - use an account without MFA for testing');
    }
    
    // Get test user ID
    const profile = await userService.getProfile();
    global.testUserId = profile.user.userId;
    
  } catch (error) {
    console.error('Integration test setup failed:', error);
    process.exit(1);
  }
});

afterAll(async () => {
  // Global cleanup if needed
});

export {}; // This makes the file a module