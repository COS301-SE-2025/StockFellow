import { Stack } from "expo-router";

export default function StokvelLayout() {
  return (
    <Stack>
      <Stack.Screen 
        name="create" 
        options={{ 
          headerShown: false,
          title: "Create Stokvel",
          headerBackTitle: "Back"
        }} 
      />
    </Stack>
  );
}