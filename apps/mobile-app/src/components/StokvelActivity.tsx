import React from "react";
import { View, Text, Image } from "react-native";
import { icons } from "../constants";
import { formatDistanceToNow } from "date-fns";

interface ActivityItem {
  id: string;
  type: "joined" | "contribution_change" | "payout" | "contribution" | "missed_contribution";
  memberName: string;
  stokvelName?: string;
  previousAmount?: number;
  newAmount?: number;
  amount?: number;
  recipientName?: string;
  timestamp: Date;
  profileImage?: string | null;
}

const StokvelActivity: React.FC<{ activity: ActivityItem }> = ({ activity }) => {
  const renderMessage = () => {
    switch (activity.type) {
      case "joined":
        return (
          <Text className="text-sm">
            <Text className="font-['PlusJakartaSans-SemiBold']">{activity.memberName}</Text> has joined{" "}
            <Text className="font-['PlusJakartaSans-SemiBold']">{activity.stokvelName}</Text>
          </Text>
        );
      case "contribution_change":
        return (
          <Text className="text-sm">
            <Text className="font-['PlusJakartaSans-SemiBold']">{activity.memberName}</Text> has changed contribution amount from{" "}
            <Text className="font-['PlusJakartaSans-Bold'] text-[#03DE58]">R{activity.previousAmount?.toFixed(2)}</Text> to{" "}
            <Text className="font-['PlusJakartaSans-Bold'] text-[#03DE58]">R{activity.newAmount?.toFixed(2)}</Text>
          </Text>
        );
      case "payout":
        return (
          <Text className="text-sm">
            Monthly payout of{" "}
            <Text className="font-['PlusJakartaSans-Bold'] text-[#03DE58]">R{activity.amount?.toFixed(2)}</Text> has been paid to{" "}
            <Text className="font-['PlusJakartaSans-SemiBold']">{activity.recipientName}</Text>
          </Text>
        );
      case "contribution":
        return (
          <Text className="text-sm">
            <Text className="font-['PlusJakartaSans-SemiBold']">{activity.memberName}</Text> has contributed{" "}
            <Text className="font-['PlusJakartaSans-Bold'] text-[#03DE58]">R{activity.amount?.toFixed(2)}</Text>
          </Text>
        );
      case "missed_contribution":
        return (
          <Text className="text-sm">
            <Text className="font-['PlusJakartaSans-SemiBold']">{activity.memberName}</Text> has missed their monthly contribution of{" "}
            <Text className="font-['PlusJakartaSans-Bold'] text-[#FF3B30]">R{activity.amount?.toFixed(2)}</Text>
          </Text>
        );
      default:
        return null;
    }
  };

  return (
    <View className="flex-row items-center my-5 mx-4">
      {/* Profile Image */}
      <View className="w-14 h-14 rounded-full items-center justify-center mr-3 bg-white shadow-md shadow-[#1DA1FA]/90">
        <Image
          source={activity.profileImage ? { uri: activity.profileImage } : icons.avatar}
          className="w-12 h-12 rounded-full"
          resizeMode="cover"
        />
      </View>

      {/* Activity Content */}
      <View className="flex-1 justify-center">
        {renderMessage()}
        <Text className="text-gray-500 text-xs mt-1 font-['PlusJakartaSans-Light']">
          {formatDistanceToNow(new Date(activity.timestamp), { addSuffix: true })}
        </Text>
      </View>
    </View>
  );
};

export default StokvelActivity;