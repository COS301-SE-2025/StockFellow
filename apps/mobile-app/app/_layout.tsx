import { useEffect, createContext, useState, useContext } from "react";
import { useFonts } from "expo-font";
import { SplashScreen, Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { useColorScheme } from "react-native";
import { TutorialProvider } from "../src/components/help/TutorialContext";
import TutorialOverlay from "../src/components/help/TutorialOverlay";

// Create a simple ThemeContext
export const ThemeContext = createContext({
  isDarkMode: false,
  toggleTheme: () => { },
  colors: {
    background: '#FFFFFF',
    text: '#000000',
    primary: '#1DA1FA',
    card: '#FFFFFF',
    border: '#F0F0F0',
  },
});

// Custom hook to use the theme
export const useTheme = () => useContext(ThemeContext);

// Prevent the splash screen from auto-hiding before asset loading is complete
SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  // Theme state
  const deviceTheme = useColorScheme();
  const [isDarkMode, setIsDarkMode] = useState(deviceTheme === 'dark');

  const toggleTheme = () => setIsDarkMode(prev => !prev);

  // Define colors based on theme
  const colors = {
    background: isDarkMode ? '#121212' : '#FFFFFF',
    text: isDarkMode ? '#FFFFFF' : '#000000',
    primary: '#1DA1FA',
    card: isDarkMode ? '#1E1E1E' : '#FFFFFF',
    border: isDarkMode ? '#333333' : '#F0F0F0',
  };

  const [fontsLoaded, fontError] = useFonts({
    "PlusJakartaSans-Bold": require("../assets/fonts/static/PlusJakartaSans-Bold.ttf"),
    "PlusJakartaSans-ExtraBold": require("../assets/fonts/static/PlusJakartaSans-ExtraBold.ttf"),
    "PlusJakartaSans-ExtraLight": require("../assets/fonts/static/PlusJakartaSans-ExtraLight.ttf"),
    "PlusJakartaSans-Light": require("../assets/fonts/static/PlusJakartaSans-Light.ttf"),
    "PlusJakartaSans-Medium": require("../assets/fonts/static/PlusJakartaSans-Medium.ttf"),
    "PlusJakartaSans-Regular": require("../assets/fonts/static/PlusJakartaSans-Regular.ttf"),
    "PlusJakartaSans-SemiBold": require("../assets/fonts/static/PlusJakartaSans-SemiBold.ttf"),
  });

  useEffect(() => {
    if (fontError) throw fontError;
    if (fontsLoaded) SplashScreen.hideAsync();
  }, [fontsLoaded, fontError]);

  if (!fontsLoaded) {
    return null;
  }

  return (
    <ThemeContext.Provider value={{ isDarkMode, toggleTheme, colors }}>
      <TutorialProvider>
        <StatusBar style={isDarkMode ? "light" : "dark"} />
        <Stack>
          <Stack.Screen
            name="splashpage"
            options={{
              headerShown: false,
              animation: "fade"
            }}
          />
          <Stack.Screen
            name="(onboarding)"
            options={{
              headerShown: false,
              animation: "fade"
            }}
          />
          <Stack.Screen
            name="(auth)"
            options={{
              headerShown: false,
              animation: "slide_from_right"
            }}
          />
          <Stack.Screen
            name="(tabs)"
            options={{
              headerShown: false,
              animation: "fade"
            }}
          />
          <Stack.Screen
            name="stokvels"
            options={{
              headerShown: false,
              animation: "fade"
            }}
          />
          <Stack.Screen
            name="transactions"
            options={{
              headerShown: false,
              animation: "fade"
            }}
          />
        </Stack>
        <TutorialOverlay />
      </TutorialProvider>
      <StatusBar style={isDarkMode ? "light" : "dark"} />
      
    </ThemeContext.Provider>
  );
}