import React from "react";
import { View, Text, Image } from "react-native";
import { icons } from "../constants";

interface MemberCardProps {
  name: string;
  role: string;
  tier: number; 
  profileImage?: string | null;
}

const MemberCard: React.FC<MemberCardProps> = ({
  name,
  role,
  tier,
  profileImage = null,
}) => {
  // Determine what to display for the role/tier
  const getRoleDisplayText = () => {
    if (role.toLowerCase() === "admin") {
      return `Admin/Tier ${tier}`;
    }
    if (role.toLowerCase() === "founder") {
      return `Founder/Tier ${tier}`;
    }
    return `Tier ${tier}`;
  };

  return (
    <View className="items-center mx-3 mb-5">  
      {/* Profile Image */}
      <View className="w-16 h-16 rounded-full bg-white items-center justify-center mb-2 shadow-lg shadow-[#1DA1FA]/90">
        <Image
          source={profileImage ? { uri: profileImage } : icons.avatar}
          className="w-14 h-14 rounded-full"
          resizeMode="cover"
        />
      </View>
      
      {/* Name */}
      <Text className="font-['PlusJakartaSans-SemiBold'] text-sm" numberOfLines={1}>
        {name}
      </Text>
      
      {/* Role/Tier */}
      <Text className="text-gray-500 text-xs font-['PlusJakartaSans-Regular'] my-1" numberOfLines={1}>
        {getRoleDisplayText()}
      </Text>
    </View>
  );
};

export default MemberCard;