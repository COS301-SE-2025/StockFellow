import { Stack } from "expo-router";

export default function StokvelLayout() {
  return (
    <Stack>
      {/* <Stack.Screen 
        name="create" 
        options={{ 
          headerShown: false,
          title: "Create Stokvel",
          headerBackTitle: "Back"
        }} 
      />
      <Stack.Screen 
        name="[stokvel]" 
        options={{ 
          headerShown: false,
          title: "Stokvel",
          headerBackTitle: "Back"
        }} 
      /> */}
      {/* <Stack.Screen name="index" options={{ headerShown: false }} /> */}
      <Stack.Screen name="create" options={{ headerShown: false }} />
      <Stack.Screen name="[stokvel]/index" options={{ headerShown: false }} />
      <Stack.Screen name="[stokvel]/requests" options={{ headerShown: false }} />
      
    </Stack>
  );
}