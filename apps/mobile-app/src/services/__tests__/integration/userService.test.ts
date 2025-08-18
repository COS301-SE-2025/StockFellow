import userService from '../../userService';
declare const global: {
  testUserId: string;
};

describe('UserService Integration Tests', () => {
  test('getServiceInfo returns service info', async () => {
    const info = await userService.getServiceInfo();
    expect(info).toHaveProperty('service');
    expect(info.service).toBe('UserService');
  });

  test('getProfile returns user profile', async () => {
    const profile = await userService.getProfile();
    expect(profile).toHaveProperty('user');
    expect(profile.user.userId).toBe(testUserId);
  });

  test('getUserById returns user details', async () => {
    const user = await userService.getUserById(testUserId);
    expect(user.userId).toBe(testUserId);
  });

  test('getUserAffordabilityTier returns tier info', async () => {
    const affordability = await userService.getUserAffordabilityTier();
    expect(affordability).toHaveProperty('userId');
    expect(affordability.userId).toBe(testUserId);
    expect(affordability).toHaveProperty('tier');
  });

  test('searchUsers finds users', async () => {
    // First get current user's name to search for
    const profile = await userService.getProfile();
    const searchName = profile.user.firstName || profile.user.username;
    
    const result = await userService.searchUsers(searchName);
    expect(result.count).toBeGreaterThan(0);
    expect(result.users.some(u => u.userId === testUserId)).toBe(true);
  });

  test('getVerifiedUsers returns verified users', async () => {
    const result = await userService.getVerifiedUsers();
    expect(Array.isArray(result.verifiedUsers)).toBe(true);
  });

  test('getUserStats returns statistics', async () => {
    const stats = await userService.getUserStats();
    expect(stats).toHaveProperty('totalUsers');
    expect(stats.totalUsers).toBeGreaterThan(0);
  });

  test('getAffordabilityStats returns affordability stats', async () => {
    const stats = await userService.getAffordabilityStats();
    expect(stats).toHaveProperty('totalUsers');
    expect(stats.totalUsers).toBeGreaterThan(0);
  });

  // Note: ID verification test would need actual file upload
  // Might want to skip this in automated tests
  test.skip('verifyID verifies user ID', async () => {
    // This would need an actual PDF file
  });
});