import React, { useEffect } from 'react';
import { View, Text, TouchableOpacity, Animated, Dimensions, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';
import { useTutorial } from '../help/TutorialContext';
import { useTheme } from '../../../app/_layout'; // added

const { width, height } = Dimensions.get('window');

const TutorialOverlay: React.FC = () => {
    const router = useRouter();
    const { 
        isActive,
        currentStep,
        totalSteps,
        getCurrentStep,
        nextStep,
        previousStep,
        endTutorial
    } = useTutorial();
    const { isDarkMode, colors } = useTheme(); // added

  const fadeAnim = React.useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (isActive) {
      const step = getCurrentStep();
      router.push(step.screen);
      
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 300,
        useNativeDriver: true,
      }).start();
    }
  }, [isActive, currentStep]);

  if (!isActive) return null;

  const step = getCurrentStep();
  const isLastStep = currentStep === totalSteps - 1;
  const isFirstStep = currentStep === 0;

  const renderOverlay = () => {
    return (
      <View className="absolute top-0 left-0 right-0 bottom-0 bg-black/50" />
    );
  };

  const getInfoPanelPosition = () => {
    const step = getCurrentStep();
    if (step.id === 'create_stokvel') {
      return 'top-32';
    }
    return 'bottom-32';
  };

  return (
    <Animated.View 
      style={[
        StyleSheet.absoluteFill,
        { opacity: fadeAnim }
      ]}
      pointerEvents="box-none"
    >
      {renderOverlay()}
      
      {/* {step.highlightStyle && (
        <View 
          style={[
            styles.highlight,
            step.highlightStyle,
            { 
              borderColor: '#1DA1FA',
              borderRadius: 25
            }
          ]} 
          pointerEvents="none"
        />
      )} */}
      
      {/* Info Panel */}
      <View className={`absolute left-6 right-6 ${getInfoPanelPosition()}`}>
        <View
          className="bg-white rounded-2xl p-6 shadow-lg"
          style={isDarkMode ? { backgroundColor: colors.card } : undefined}
        >
          <Text
            className="text-[#1DA1FA] text-2xl font-['PlusJakartaSans-Bold'] mb-3"
            // accent color stays the same for both modes
          >
            {step.title}
          </Text>
          
          <Text
            className="text-gray-700 text-base font-['PlusJakartaSans-Regular'] mb-6"
            style={isDarkMode ? { color: colors.text, opacity: 0.85 } : undefined}
          >
            {step.description}
          </Text>
          
          <View className="flex-row justify-center mb-6">
            {Array.from({ length: totalSteps }).map((_, index) => (
              <View
                key={index}
                className={`h-2 rounded-full mx-1 ${
                  index === currentStep ? 'bg-[#1DA1FA] w-6' : 'bg-gray-200 w-2'
                }`}
                style={
                  index === currentStep
                    ? undefined
                    : isDarkMode
                      ? { backgroundColor: 'rgba(255,255,255,0.2)' }
                      : undefined
                }
              />
            ))}
          </View>
          
          <View className="flex-row justify-between">
            <View className="flex-row">
              {!isFirstStep && (
                <TouchableOpacity
                  onPress={previousStep}
                  className="px-6 py-3 rounded-full bg-gray-100"
                  style={isDarkMode ? { backgroundColor: 'rgba(255,255,255,0.1)' } : undefined}
                >
                  <Text
                    className="text-gray-700 font-['PlusJakartaSans-Medium']"
                    style={isDarkMode ? { color: colors.text } : undefined}
                  >
                    Back
                  </Text>
                </TouchableOpacity>
              )}
            </View>
            
            <View className="flex-row">
              <TouchableOpacity
                onPress={endTutorial}
                className="px-6 py-3 rounded-full mr-2"
              >
                <Text
                  className="text-gray-500 font-['PlusJakartaSans-Medium']"
                  style={isDarkMode ? { color: '#9CA3AF' } : undefined}
                >
                  Skip
                </Text>
              </TouchableOpacity>
              
              <TouchableOpacity
                onPress={nextStep}
                className="px-6 py-3 rounded-full bg-[#1DA1FA]"
              >
                <Text className="text-white font-['PlusJakartaSans-Medium']">
                  {isLastStep ? 'Finish' : 'Next'}
                </Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </View>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  overlaySection: {
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
  },
  highlight: {
    position: 'absolute',
    borderWidth: 2,
    borderStyle: 'dashed',
    backgroundColor: 'transparent',
    zIndex: 51
  }
});

export default TutorialOverlay;