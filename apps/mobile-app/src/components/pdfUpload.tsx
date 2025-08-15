import { Image, Text, TouchableOpacity, View, Alert, ActivityIndicator } from 'react-native';
import React, { useState } from 'react';
import { icons } from '../constants';
import * as DocumentPicker from 'expo-document-picker';

interface PDFUploadProps {
  heading: string;
  onDocumentSelect: (document: DocumentPicker.DocumentPickerAsset) => void;
}

const PDFUpload: React.FC<PDFUploadProps> = ({ heading, onDocumentSelect }) => {
  const [uploadedDocument, setUploadedDocument] = useState<DocumentPicker.DocumentPickerAsset | null>(null);
  const [loading, setLoading] = useState(false);

  const upload = async () => {
    try {
      setLoading(true);
      const res = await DocumentPicker.getDocumentAsync({
        type: 'application/pdf',
      });

      if (res.canceled) {
        console.log('File selection canceled');
        return;
      }

      if (res.assets && res.assets[0]) {
        setUploadedDocument(res.assets[0]);
        onDocumentSelect(res.assets[0]);
      }
    } catch (err) {
      console.error('Error during document picker:', err);
      Alert.alert('Error', 'Failed to select document. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View className="flex-1 mb-4 py-4">
      <Text className="text-base py-3 text-[#0C0C0F]">
        {heading}
      </Text>
      <TouchableOpacity
        onPress={upload}
        activeOpacity={0.7}
        className={`py-8 w-full h-full justify-center items-center gap-2 flex-0 border-2 border-dashed border-[#1DA1FA] rounded-[30px] ${
          loading ? 'opacity-70' : ''
        }`}
        disabled={loading}
      >
        {loading ? (
          <ActivityIndicator size="large" color="#1DA1FA" />
        ) : (
          <>
            <Image
              source={uploadedDocument ? icons.uploaded : icons.pdf}
              className="w-15 h-15"
              resizeMode="contain"
              //tintColor={'#1DA1FA'}
            />
            <Text className="text-center">
              {uploadedDocument ? (
                <Text className="text-[#009963] font-medium">
                  Uploaded {uploadedDocument.name}
                </Text>
              ) : (
                <>
                  Drag and drop or{' '}
                  <Text className="text-[#1DA1FA]">Choose</Text> PDF file
                </>
              )}
            </Text>
          </>
        )}
      </TouchableOpacity>
    </View>
  );
};

export default PDFUpload;