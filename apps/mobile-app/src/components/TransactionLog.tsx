// TransactionLog.tsx
import React from 'react';
import { View, Text, Image } from 'react-native';
import { icons } from '../../src/constants';

export interface Transaction {
  id: string;
  type: 'contribution' | 'payout';
  amount: number;
  groupName: string;
  date: string;
  profileImage?: string | null;
}

interface TransactionLogProps {
  transactions: Transaction[];
}

const TransactionLog: React.FC<TransactionLogProps> = ({ transactions }) => {
  const formatDate = (dateString: string) => {
    const options: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short', year: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-GB', options);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-ZA', {
      style: 'currency',
      currency: 'ZAR',
      minimumFractionDigits: 2,
    }).format(amount);
  };

  return (
    <View className="w-full">
      {transactions.map((transaction) => (
        <View 
          key={transaction.id}
          className="flex-row items-center justify-between py-4 border-b border-gray-100"
        >
          <Image
            source={transaction.profileImage ? { uri: transaction.profileImage } : icons.stokvelpfp}
            className="w-12 h-12 rounded-full"
            resizeMode="cover"
            defaultSource={icons.stokvelpfp}
          />

          <View className="flex-1 ml-3">
            <Text className="text-gray-500 text-xs">
              {formatDate(transaction.date)}
            </Text>
            <Text className="text-black font-medium mt-1">
              {transaction.groupName}
            </Text>
          </View>

          <Text 
            className={`font-semibold ${
              transaction.type === 'payout' ? 'text-green-500' : 'text-red-500'
            }`}
          >
            {transaction.type === 'payout' ? '+' : '-'} {formatCurrency(transaction.amount)}
          </Text>
        </View>
      ))}
    </View>
  );
};

export default TransactionLog;