// app/(tabs)/_layout.tsx
import { Tabs } from 'expo-router';

export default function TabsLayout() {
  return (
    <Tabs>
      <Tabs.Screen name="home" options={{ title: 'Home' }} />
      <Tabs.Screen name="profile" options={{ title: 'Profile' }} />
      <Tabs.Screen name="stokvels" options={{ title: 'Stokvels' }} />
      <Tabs.Screen name="transactions" options={{ title: 'Transactions' }} />
    </Tabs>
  );
}
