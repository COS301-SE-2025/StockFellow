import React, { useState } from "react";
import { View, TouchableOpacity, Modal, Text, Image, Alert } from "react-native";
import * as ImagePicker from 'expo-image-picker';
import { icons } from "../constants";
import CustomButton from './CustomButton';

interface AvatarProps {
  profileImage: string | null;
  onImageUpdate: (uri: string | null) => void;
}

const StokvelAvatar: React.FC<AvatarProps> = ({ profileImage, onImageUpdate }) => {
  const [modalVisible, setModalVisible] = useState(false);
  const [currentImage, setCurrentImage] = useState<string | null>(profileImage);

  const openModal = () => setModalVisible(true);
  const closeModal = () => setModalVisible(false);

  const takePhoto = async () => {
    const permission = await ImagePicker.requestCameraPermissionsAsync();
    if (!permission.granted) {
      Alert.alert("Permission required", "Camera access is required to take a photo.");
      return;
    }

    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.7,
      base64: true,
    });

    if (!result.canceled && result.assets[0].uri) {
      const imageUri = result.assets[0].uri;
      setCurrentImage(imageUri);
      onImageUpdate(imageUri);
    }
    closeModal();
  };

  const selectFromGallery = async () => {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      Alert.alert("Permission required", "Gallery access is required to select a photo.");
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.7,
      base64: true,
    });

    if (!result.canceled && result.assets[0].uri) {
      const imageUri = result.assets[0].uri;
      setCurrentImage(imageUri);
      onImageUpdate(imageUri);
    }
    closeModal();
  };

  const removeImage = () => {
    setCurrentImage(null);
    onImageUpdate(null);
    closeModal();
  };

  return (
    <View className="items-center justify-center">
      <TouchableOpacity onPress={openModal} className="relative mb-4">
        <Image
          source={currentImage ? { uri: currentImage } : icons.stokvelpfp}
          className="w-45 h-45 rounded-full shadow-2xl shadow-black/60"
          resizeMode="contain"
        />
        <View className=" bg-[#1DA1FA] absolute bottom-0 right-2 p-2 rounded-full shadow-lg shadow-black/20 "
        style={{ elevation: 5 }}
        >
          <Image
            source={icons.camera}
            className="w-8 h-8"
            resizeMode="contain"
          />
        </View>
      </TouchableOpacity>

      <Modal
        visible={modalVisible}
        transparent
        animationType="fade"
        onRequestClose={closeModal}
      >
        <View className="flex-1 justify-center items-center bg-black/50">
          <View className="bg-white w-4/5 p-5 rounded-3xl my-[60%]">
            <Text className="text-xl font-bold m-2 text-center">Change Profile Picture</Text>
            <View className="flex-row justify-center items-center my-5">
              <TouchableOpacity onPress={takePhoto} className="items-center mx-4">
                <Image source={icons.camera} className="w-9 h-9 " tintColor={"#0C0C0F"} />
                <Text className="text-gray-700 my-1">Camera</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={selectFromGallery} className="items-center mx-4">
                <Image source={icons.gallery} className="w-9 h-9" />
                <Text className="text-gray-700 my-1">Gallery</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={removeImage} className="items-center mx-4">
                <Image source={icons.trash} className="w-9 h-9" tintColor={"#dc2626"} />
                <Text className="text-red-600 my-1">Remove</Text>
              </TouchableOpacity>
            </View>

            <CustomButton
              title="Cancel"
              containerStyles="bg-black rounded-3xl px-5 py-3 mb-4 self-center"
              textStyles="text-white text-base"
              handlePress={closeModal}
              isLoading={false}
            />
          </View>
        </View>
      </Modal>
    </View>
  );
};

export default StokvelAvatar;