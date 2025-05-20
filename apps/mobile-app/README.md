## Getting Started

### Prerequisites

- **Node.js**: v18 or later ([Download](https://nodejs.org/en/download/)).
- **Git**: For cloning the repository.
- **Expo CLI**: `npm install -g expo-cli`.
- **Expo Go App**: For testing on iOS/Android devices.
- **Emulator/Simulator** (optional): Android Studio (Android) or Xcode (iOS, macOS only).
- **Editor**: VS Code with ESLint/Prettier extensions.

### Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd stockfellow
   ```

2. **Install Node.js**:
   - Verify: `node --version` (should show v18.x.x).
   - If needed, install via [nodejs.org](https://nodejs.org/en/download/) or:
     ```bash
     nvm install 18
     nvm use 18
     ```

3. **Install Expo CLI**:
   ```bash
   npm install -g expo-cli
   expo --version
   ```

4. **Install Dependencies**:
   ```bash
   npm install
   ```
   - Installs dependencies for all workspaces (`apps/`, `services/`, `packages/`).

5. **Set Up Environment Variables**:
   - Copy `apps/mobile-app/.env.example` to `apps/mobile-app/.env` if present.
   - Add required values (e.g., API URLs) from the team lead.

6. **Start the Mobile App**:
   ```bash
   cd apps/mobile-app
   expo start
   ```
   - Test with Expo Go (scan QR code) or emulator (`a` for Android, `i` for iOS).

7. **Run Tests**:
   ```bash
   npm test --workspace=apps/mobile-app
   npm run test:cypress --workspace=apps/mobile-app
   ```
## Troubleshooting

- **Version Conflicts**: Check with `npm ls react-native` and align versions in `package.json`.
- **Metro Slow**: Add `apps/mobile-app/metro.config.js`:
  ```javascript
  const { getDefaultConfig } = require('expo/metro-config');
  const path = require('path');

  const config = getDefaultConfig(__dirname);
  config.watchFolders = [
    path.resolve(__dirname, '../..'),
    path.resolve(__dirname, '../../packages')
  ];
  config.resolver.nodeModulesPaths = [
    path.resolve(__dirname, 'node_modules'),
    path.resolve(__dirname, '../..', 'node_modules')
  ];
  module.exports = config;
  ```
- **Expo Go Fails**: Use `expo start --tunnel`.

## Resources

- [Expo Documentation](https://docs.expo.dev/)
- [React Native Documentation](https://reactnative.dev/)
- [npm Workspaces](https://docs.npmjs.com/cli/v7/using-npm/workspaces/)