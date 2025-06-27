import React, { useEffect } from 'react';
import { View, Text, TouchableOpacity, Animated, Dimensions, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';
import { useTutorial } from '../help/TutorialContext';

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
      <View className="absolute top-0 left-0 right-0 bottom-0 bg-black/50" /> // Changed from /70 to /50
    );
  };

  // Adjust info panel position based on current step
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
        <View className="bg-white rounded-2xl p-6 shadow-lg">
          <Text className="text-[#1DA1FA] text-2xl font-['PlusJakartaSans-Bold'] mb-3">
            {step.title}
          </Text>
          
          <Text className="text-gray-700 text-base font-['PlusJakartaSans-Regular'] mb-6">
            {step.description}
          </Text>
          
          <View className="flex-row justify-center mb-6">
            {Array.from({ length: totalSteps }).map((_, index) => (
              <View
                key={index}
                className={`h-2 rounded-full mx-1 ${
                  index === currentStep ? 'bg-[#1DA1FA] w-6' : 'bg-gray-200 w-2'
                }`}
              />
            ))}
          </View>
          
          <View className="flex-row justify-between">
            <View className="flex-row">
              {!isFirstStep && (
                <TouchableOpacity
                  onPress={previousStep}
                  className="px-6 py-3 rounded-full bg-gray-100"
                >
                  <Text className="text-gray-700 font-['PlusJakartaSans-Medium']">Back</Text>
                </TouchableOpacity>
              )}
            </View>
            
            <View className="flex-row">
              <TouchableOpacity
                onPress={endTutorial}
                className="px-6 py-3 rounded-full mr-2"
              >
                <Text className="text-gray-500 font-['PlusJakartaSans-Medium']">Skip</Text>
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