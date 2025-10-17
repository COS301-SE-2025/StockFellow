import { View, Text, Image } from 'react-native';
import { icons, images } from '../../src/constants';

const DebitCard = ({ 
  bankName = "First National Bank",
  cardNumber = "•••• •••• •••• 1234",
  cardHolder = "L SMITH",
  expiryDate = "10/26",
  cardType = "mastercard" // or "visa", "paypal", etc.
}) => {
  // Bank logo mapping
  const bankLogos = [
    { name: 'Standard Bank', logo: icons.standardbank },
    { name: 'Absa', logo: icons.absa },
    { name: 'Capitec Bank', logo: icons.capitec },
    { name: 'First National Bank', logo: icons.fnb },
    { name: 'Nedbank', logo: icons.nedbank }
  ];

  // Determine which card type logo to use
  const cardTypeLogo = () => {
    switch(cardType.toLowerCase()) {
      case 'mastercard':
        return icons.mastercard; 
      case 'visa':
        return icons.visa;
      
      default:
        return icons.mastercard;
    }
  };

  // Find the bank logo based on bankName
  const getBankLogo = () => {
    const bank = bankLogos.find(item => 
      item.name.toLowerCase() === bankName.toLowerCase()
    );
    return bank ? bank.logo : icons.fnb; // Default to FNB if not found
  };

  return (
    <View className="w-full h-[200px] relative mb-6 ">
      {/* Card Background Image */}
      <Image 
        source={images.card} // Your card background image
        className="w-full h-full absolute rounded-2xl shadow-xl shadow-[#0C0C0F]/100"
        resizeMode="cover"
      />
      
      {/* Card Content */}
      <View className="p-5 h-full flex flex-col justify-between">
        {/* Top Row - Bank Name and Logo */}
        <View className="flex-row justify-between items-start">
          <Text className="text-white font-semibold text-lg">{bankName}</Text>
          <Image 
            source={getBankLogo()}
            className="w-12 h-12"
            resizeMode="contain"
          />
        </View>
        
        {/* Middle - Card Number */}
        <View className="flex-row justify-center">
          <Text className="text-white text-xl tracking-widest">
            {cardNumber}
          </Text>
        </View>
        
        {/* Bottom Row - Card Details */}
        <View className="flex-row justify-between items-end">
          {/* Cardholder Name */}
          <View>
            <Text className="text-white text-xs opacity-80">Cardholder Name</Text>
            <Text className="text-white font-medium">{cardHolder}</Text>
          </View>
          
          {/* Expiry Date */}
          <View className="ml-4">
            <Text className="text-white text-xs opacity-80">Expiry Date</Text>
            <Text className="text-white font-medium">{expiryDate}</Text>
          </View>
          
          {/* Card Type Logo */}
          <Image 
            source={cardTypeLogo()}
            className="w-12 h-10"
            resizeMode="contain"
          />
        </View>
      </View>
    </View>
  );
};

export default DebitCard;