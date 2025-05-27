import { StyleSheet, Text, View } from 'react-native'
import React from 'react'
import CustomButton from "../../src/components/CustomButton";
import { router } from 'expo-router';

const stokvels = () => {
  return (
    <View className='flex-1 justify-center items-center'>
      <Text>stokvels</Text>
      <CustomButton
              title="Create"
              containerStyles="bg-black rounded-full py-4 px-6 my-6  self-center"
              textStyles="text-white text-base font-normal"
              handlePress={()=>{router.push('/stokvels/create')}}
              
            />
    </View>
  )
}

export default stokvels

const styles = StyleSheet.create({})