// apps/mobile-app/app/transactions/_layout.tsx
import { Stack } from 'expo-router';

export default function TransactionsLayout() {
  return (
    <Stack>
      <Stack.Screen name="cardform" options={{ headerShown: false }} />
      <Stack.Screen name="cards" options={{ headerShown: false }} />
    </Stack>
  );
}