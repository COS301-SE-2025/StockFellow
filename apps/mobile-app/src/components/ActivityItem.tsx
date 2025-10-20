import React from 'react';
import { View, Text, TouchableOpacity, Image } from 'react-native';
import { useTheme } from '../../app/_layout';
import icons from '../constants/icons';

interface ActivityItemProps {
  title: string;
  subtitle: string;
  amount?: string;
  onPress: () => void;
  icon?: React.ReactNode;
  timestamp?: string;
}

const chevronRight = icons.up;

const ActivityItem: React.FC<ActivityItemProps> = ({
  title,
  subtitle,
  amount,
  onPress,
  icon,
  timestamp
}) => {
  const { colors } = useTheme();

  return (
    <TouchableOpacity
      onPress={onPress}
      style={{
        backgroundColor: '#E6F2FF',
        borderRadius: 14,
        padding: 12,
        marginBottom: 10,
        flexDirection: 'row',
        alignItems: 'center',
        // Removed shadow for a cleaner look
      }}
      activeOpacity={0.85}
    >
      {/* Left Section */}
      <View style={{ flex: 1, flexDirection: 'row', alignItems: 'center' }}>
        {/* Optional icon */}
        {icon && (
          <View style={{ marginRight: 8 }}>
            {icon}
          </View>
        )}
        <View style={{ flex: 1 }}>
          <Text
            style={{
              fontSize: 15,
              fontWeight: '700',
              color: colors.text,
              marginBottom: 1,
              fontFamily: 'PlusJakartaSans-SemiBold',
            }}
            numberOfLines={1}
          >
            {title}
          </Text>
          <Text
            style={{
              fontSize: 13,
              color: colors.text,
              opacity: 0.7,
              fontFamily: 'PlusJakartaSans-Regular',
              marginBottom: 1,
            }}
            numberOfLines={1}
          >
            {subtitle}
          </Text>
          {timestamp && (
            <Text
              style={{
                fontSize: 11,
                color: colors.text,
                opacity: 0.4,
                fontFamily: 'PlusJakartaSans-Regular',
              }}
            >
              {timestamp}
            </Text>
          )}
        </View>
      </View>

      {/* Divider */}
      <View style={{
        width: 1,
        height: 32,
        backgroundColor: '#B3D8FF',
        marginHorizontal: 10,
        borderRadius: 1,
      }} />

      {/* Right Section - Amount & Chevron */}
      <View style={{ alignItems: 'flex-end', flexDirection: 'row' }}>
        {amount && (
          <Text
            style={{
              fontSize: 16,
              fontWeight: 'bold',
              color: colors.primary,
              marginRight: 6,
              fontFamily: 'PlusJakartaSans-Bold',
            }}
          >
            {amount.startsWith('R') ? amount : `R${amount}`}
          </Text>
        )}
        <Image
          source={chevronRight}
          style={{ width: 18, height: 18, tintColor: '#1DA1FA' }}
        />
      </View>
    </TouchableOpacity>
  );
};

export default ActivityItem;