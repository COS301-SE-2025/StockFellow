// import React from 'react';
// import { View, Text, TouchableOpacity, Image } from 'react-native';
// import { useRouter } from 'expo-router';
// import { icons } from '../constants';
// import { useTheme } from '../../app/_layout';
// import HelpButton from './help/HelpButton';

// interface TopBarProps {
//   title?: string;
//   showBackButton?: boolean;
//   rightComponent?: React.ReactNode;
//   onBackPress?: () => void;
// }

// const TopBar: React.FC<TopBarProps> = ({
//   title,
//   showBackButton = false,
//   rightComponent,
//   onBackPress,
// }) => {
//   const router = useRouter();
//   const { isDarkMode, toggleTheme, colors } = useTheme();

//   const handleBackPress = () => {
//     if (onBackPress) {
//       onBackPress();
//     } else {
//       router.back();
//     }
//   };

//   return (
//     <View 
//       style={{ backgroundColor: colors.background }} 
//       className="w-full px-6 flex-row justify-between items-center"
//     >
//       <View className="flex-row items-center">
//         {showBackButton && (
//           <TouchableOpacity onPress={handleBackPress} className="mr-4">
//             <Image
//               source={icons.back}
//               style={{ tintColor: colors.text }}
//               className="w-6 h-6"
//             />
//           </TouchableOpacity>
//         )}
//         {title && (
//           <Text style={{ color: colors.text }} className="text-lg font-['PlusJakartaSans-SemiBold']">
//             {title}
//           </Text>
//         )}
//       </View>

//       <View className="flex-row items-center">
        

//         {rightComponent}

//         {/* Help Button */}
//         <HelpButton />
        
//         {/* Theme Toggle Button */}
//         <TouchableOpacity
//           onPress={toggleTheme}
//           className="p-2"
//         >
//           <Image 
//             source={icons.light}
//             style={{ 
//               width: 28,
//               height: 28,
//               tintColor: isDarkMode ? '#FFFFFF' : '#000000'
//             }}
//             resizeMode="contain"
//           />
//         </TouchableOpacity>

//         {/* Notifications Button */}
//         <TouchableOpacity 
//           onPress={() => router.push('/notifications')}
//           className="p-2 mr-2"
//         >
//           <Image 
//             source={icons.bell_filled}
//             style={{ 
//               width: 24,
//               height: 24,
//               // tintColor: colors.text
//             }}
//             resizeMode="contain"
//           />
//         </TouchableOpacity>
//       </View>
//     </View>
//   );
// };

// export default TopBar;




import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, Image } from 'react-native';
import { useRouter } from 'expo-router';
import { useFocusEffect } from '@react-navigation/native';
import { icons } from '../constants';
import { useTheme } from '../../app/_layout';
import HelpButton from './help/HelpButton';
import NotificationService from '../services/notificationService';

interface TopBarProps {
  title?: string;
  showBackButton?: boolean;
  rightComponent?: React.ReactNode;
  onBackPress?: () => void;
  rightAction?: {
    text: string;
    onPress: () => void;
  };
}

const TopBar: React.FC<TopBarProps> = ({
  title,
  showBackButton = false,
  rightComponent,
  onBackPress,
  rightAction,
}) => {
  const router = useRouter();
  const { isDarkMode, toggleTheme, colors } = useTheme();
  const [unreadCount, setUnreadCount] = useState(0);

  const handleBackPress = () => {
    if (onBackPress) {
      onBackPress();
    } else {
      router.back();
    }
  };

  // Load unread notification count
  const loadUnreadCount = async () => {
    try {
      const response = await NotificationService.getUnreadCount();
      setUnreadCount(response.unreadCount);
    } catch (error) {
      console.error('Failed to load unread count:', error);
      setUnreadCount(0);
    }
  };

  // Load unread count when component mounts and when screen is focused
  useFocusEffect(
    React.useCallback(() => {
      loadUnreadCount();
    }, [])
  );

  return (
    <View 
      style={{ backgroundColor: colors.background }} 
      className="w-full px-6 flex-row justify-between items-center"
    >
      <View className="flex-row items-center">
        {showBackButton && (
          <TouchableOpacity onPress={handleBackPress} className="mr-4">
            <Image
              source={icons.back}
              style={{ tintColor: colors.text }}
              className="w-6 h-6"
            />
          </TouchableOpacity>
        )}
        {title && (
          <Text style={{ color: colors.text }} className="text-lg font-['PlusJakartaSans-SemiBold']">
            {title}
          </Text>
        )}
      </View>

      <View className="flex-row items-center">
        {/* Right Action Button (e.g., Mark All Read) */}
        {rightAction && (
          <TouchableOpacity onPress={rightAction.onPress} className="mr-4">
            <Text className="text-blue-500 font-['PlusJakartaSans-Medium']">
              {rightAction.text}
            </Text>
          </TouchableOpacity>
        )}

        {rightComponent}

        {/* Help Button */}
        <HelpButton />
        
        {/* Theme Toggle Button */}
        <TouchableOpacity
          onPress={toggleTheme}
          className="p-2"
        >
          <Image 
            source={icons.light}
            style={{ 
              width: 28,
              height: 28,
              tintColor: isDarkMode ? '#FFFFFF' : '#000000'
            }}
            resizeMode="contain"
          />
        </TouchableOpacity>

        {/* Notifications Button with Badge */}
        <TouchableOpacity 
          onPress={() => router.push('/notifications')}
          className="p-2 mr-2 relative"
        >
          <Image 
            source={icons.bell_filled}
            style={{ 
              width: 24,
              height: 24,
            }}
            resizeMode="contain"
          />
          {unreadCount > 0 && (
            <View className="absolute -top-1 -right-1 bg-red-500 rounded-full min-w-[18px] h-[18px] justify-center items-center">
              <Text className="text-white text-xs font-['PlusJakartaSans-Bold']">
                {unreadCount > 99 ? '99+' : unreadCount.toString()}
              </Text>
            </View>
          )}
        </TouchableOpacity>
      </View>
    </View>
  );
};

export default TopBar;