import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image } from 'react-native';

const Verification = () => {
  return (
    <View style={styles.container}>
      {/* Top Bar with Back Button */}
      <View style={styles.topBar}>
        <TouchableOpacity>
          <Image
            source={require('../../assets/icons/back.png')}
            style={styles.backButton}
          />
        </TouchableOpacity>
      </View>

      {/* Title */}
      <Text style={styles.title}>Biometric Verification</Text>
      <Text style={styles.subtitle}>
        We will confirm your identity using biometrics.
      </Text>

      {/* Face image with instructions */}
      <View style={styles.imageContainer}>
        <Image
          source={require('../../assets/icons/face.png')}
          style={styles.largeImage}
        />
        <Text style={styles.imageText}>Position your face in the frame</Text>
      </View>

      {/* Guidelines */}
      <View style={styles.guidelinesContainer}>
        <View style={styles.guideline}>
          <Image
            source={require('../../assets/icons/no-glasses.png')}
            style={styles.guidelineIcon}
          />
          <Text style={styles.guidelineText}>Avoid Wearing Glasses</Text>
        </View>
        <View style={styles.guideline}>
          <Image
            source={require('../../assets/icons/no-hat.png')}
            style={styles.guidelineIcon}
          />
          <Text style={styles.guidelineText}>Avoid Wearing Hats</Text>
        </View>
        <View style={styles.guideline}>
          <Image
            source={require('../../assets/icons/avoid-objects.png')}
            style={styles.guidelineIcon}
          />
          <Text style={styles.guidelineText}>Avoid Background Objects</Text>
        </View>
        <View style={styles.guideline}>
          <Image
            source={require('../../assets/icons/light.png')}
            style={styles.guidelineIcon}
          />
          <Text style={styles.guidelineText}>Ensure Good Lighting in Room</Text>
        </View>
      </View>

      {/* Start Verification Button */}
      <View style={styles.bottomPrompt}>
        <TouchableOpacity style={styles.verificationButton}>
          <Text style={styles.buttonText}>Start Verification</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

export default Verification;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
    paddingHorizontal: 24,
    paddingTop: 40,
  },
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 32,
  },
  backButton: {
    width: 24,
    height: 24,
    tintColor: '#1DA1FA',
  },
  title: {
    fontSize: 26,
    fontFamily: 'PlusJakartaSans-Bold',
    textAlign: 'center',
    marginBottom: 16,
    color: '#111827',
  },
  subtitle: {
    fontSize: 16,
    // fontFamily: 'PlusJakartaSans-Regular',
    color: '#6b7280',
    textAlign: 'center',
    marginBottom: 40,
  },
  imageContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 40,
  },
  largeImage: {
    width: 200,
    height: 200,
    marginBottom: 24,
    tintColor: '#1DA1FA',
  },
  imageText: {
    fontSize: 18,
    // fontFamily: 'PlusJakartaSans-SemiBold',
    color: '#374151',
    textAlign: 'center',
    marginTop: 16,
  },
  guidelinesContainer: {
    marginBottom: 40,
  },
  guideline: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  guidelineIcon: {
    width: 24,
    height: 24,
    marginRight: 12,
    tintColor: '#1DA1FA',
  },
  guidelineText: {
    fontSize: 16,
    // fontFamily: 'PlusJakartaSans-Medium', 
    color: '#374151',
  },
  bottomPrompt: {
    position: 'absolute',
    bottom: 40,
    left: 0,
    right: 0,
    alignItems: 'center',
  },
  verificationButton: {
    backgroundColor: '#1DA1FA',
    paddingVertical: 15,
    paddingHorizontal: 40,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonText: {
    fontSize: 16,
    // fontFamily: 'PlusJakartaSans-SemiBold',
    color: '#ffffff',
  },
});