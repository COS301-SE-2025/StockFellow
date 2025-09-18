import React from 'react'
import { Stack } from 'expo-router'

const AuthLayout = () => {
  return (
    <>
      <Stack>
        <Stack.Screen
          name='signup'
          options={{
            headerShown: false
          }}
        />
        <Stack.Screen
          name='login'
          options={{
            headerShown: false
          }}
        />
        <Stack.Screen
          name='verification'
          options={{
            headerShown: false
          }}
        />
        <Stack.Screen
          name='mfaVerification'
          options={{
            headerShown: false
          }}
        />
        <Stack.Screen
          name='biometricRegistration'
          options={{
            headerShown: false
          }}
        />
      </Stack>
    </>
  )
}

export default AuthLayout
