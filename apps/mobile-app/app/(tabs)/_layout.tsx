// app/(tabs)/_layout.tsx
import { View, Image,  Platform, Dimensions } from 'react-native'
import { Tabs } from 'expo-router'
import icons from '../../src/constants/icons'

const { height } = Dimensions.get('window')
interface TabIconProps {
  icon: any; // Consider using a more specific type for your icons
  color: string;
  focused: boolean;
  value?: any; // Made optional since it's not being used
}

const TabIcon = ({ icon, color, focused }: TabIconProps) => {
  return (
    <View className="items-center justify-center gap-1">
      <Image
        source={icon}
        resizeMode="contain"
        tintColor={color}
        className="w-8 h-8 mb-2"
      />
    </View>
  )
}

const TabsLayout = () => {
  // Calculate bottom padding based on platform
  const bottomPadding = Platform.select({
    ios: height > 800 ? 34 : 20, // More padding for iPhone X+ models
    android: 10,
    default: 0
  })

  return (
    <Tabs
      screenOptions={{
        tabBarShowLabel: true,
        // tabBarActiveTintColor: '#0C0C0F',
        tabBarInactiveTintColor: '#0C0C0F',
        tabBarStyle: {
          backgroundColor: '#F5F5F5',
          borderTopWidth: 1,
          borderTopColor: '#F5F5F5',
          height: 100 + bottomPadding, // Base height + bottom padding
          paddingTop: 5,
          paddingBottom: bottomPadding,
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0
        },
        tabBarItemStyle: {
          paddingVertical: 8
        }
      }}
    >
      <Tabs.Screen
        name="home"
        options={{
          title: "Home",
          headerShown: false,
          tabBarIcon: ({ color, focused }) => (
            <TabIcon
              icon={focused ? icons.home_filled : icons.home}
              color={color}
              focused={focused}
            />
          )
        }}
      />

      <Tabs.Screen
        name="stokvels"
        options={{
          title: "Stokvels",
          headerShown: false,
          tabBarIcon: ({ color, focused }) => (
            <TabIcon
              icon={focused ? icons.stokvel_filled : icons.stokvel}
              color={color}
              focused={focused}
            />
          )
        }}
      />

      <Tabs.Screen
        name="transactions"
        options={{
          title: "Transactions",
          headerShown: false,
          tabBarIcon: ({ color, focused }) => (
            <TabIcon
              icon={focused ? icons.transactions_filled : icons.transactions}
              color={color}
              focused={focused}
            />
          )
        }}
      />

      <Tabs.Screen
        name="profile"
        options={{
          title: "Profile",
          headerShown: false,
          tabBarIcon: ({ color, focused }) => (
            <TabIcon
              icon={focused ? icons.profile_filled : icons.profile}
              color={color}
              focused={focused}
            />
          )
        }}
      />
    </Tabs>
  )
}

export default TabsLayout