import React, { useState } from 'react';
import { TouchableOpacity, Image } from 'react-native';
import { icons } from '../../constants';
import { useTheme } from '../../../app/_layout';
import HelpMenu from './HelpMenu';

const HelpButton: React.FC = () => {
  const { isDarkMode } = useTheme();
  const [showMenu, setShowMenu] = useState(false);
  
  return (
    <>
      <TouchableOpacity
        onPress={() => setShowMenu(true)}
        className="p-2"
      >
        <Image 
          source={icons.help}
          style={{ 
            width: 24,
            height: 24,
            tintColor: isDarkMode ? '#FFFFFF' : '#000000'
          }}
          resizeMode="contain"
        />
      </TouchableOpacity>

      <HelpMenu 
        isVisible={showMenu}
        onClose={() => setShowMenu(false)}
      />
    </>
  );
};

export default HelpButton;