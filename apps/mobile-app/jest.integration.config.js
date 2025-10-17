require('dotenv').config({ path: '.env.test' });

module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/src/services/__tests__/integration/**/*.test.ts'],
  setupFilesAfterEnv: ['<rootDir>/src/services/__tests__/integration/setup.ts'],
  
  // Transform only TypeScript files
  transform: {
    '^.+\\.(ts|tsx)$': 'ts-jest',
  },
  
  // Don't transform node_modules
  transformIgnorePatterns: [
    'node_modules/',
  ],
  
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
    // Mock React Native and Expo modules
    '^react-native$': '<rootDir>/__mocks__/react-native.js',
    '^expo-secure-store$': '<rootDir>/__mocks__/expo-secure-store.js',
    '^@react-native-async-storage/async-storage$': '<rootDir>/__mocks__/async-storage.js',
  },
  
  moduleFileExtensions: ['ts', 'tsx', 'js', 'json', 'node'],
};