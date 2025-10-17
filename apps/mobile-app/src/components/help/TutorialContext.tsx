import React, { createContext, useContext, useState } from 'react';
import { ViewStyle } from 'react-native';

export interface TutorialStep {
  id: string;
  title: string;
  description: string;
  screen: string;
  targetElement?: string;
  position?: 'top' | 'bottom' | 'left' | 'right' | 'center';
  highlightStyle?: {
    width: number;
    height: number;
    top?: number;
    bottom?: number;  
    right?: number;
    alignSelf?: 'center';
  };
}

const tutorialSteps: TutorialStep[] = [
  {
    id: 'welcome',
    title: 'Welcome to StockFellow',
    description: 'Let\'s take a quick tour of your stokvel management platform.',
    screen: '/(tabs)/home',
    position: 'center'
  },
  {
    id: 'create_stokvel',
    title: 'Create a Stokvel',
    description: 'Start your own stokvel group by tapping the \t"+ Create" button.',
    screen: '/(tabs)/stokvels',
    targetElement: 'create-stokvel-button',
    position: 'bottom',
  },
  {
    id: 'search_stokvels',
    title: 'Find Stokvels',
    description: 'Search for the stokvel groups you are part of, or for existing stokvel groups to join.',
    screen: '/(tabs)/stokvels',
    targetElement: 'search-bar',
    position: 'top',
  },
  {
    id: 'transactions',
    title: 'Track Transactions',
    description: 'View your contributions and payouts here.',
    screen: '/(tabs)/transactions',
    targetElement: 'transactions_tab',
    position: 'bottom'
  },
  {
    id: 'profile',
    title: 'Your Profile',
    description: 'View your tier and achievements. Manage your account settings and preferences.',
    screen: '/(tabs)/profile',
    targetElement: 'profile_tab',
    position: 'bottom'
  }
];

interface TutorialContextType {
  isActive: boolean;
  currentStep: number;
  totalSteps: number;
  startTutorial: () => void;
  endTutorial: () => void;
  nextStep: () => void;
  previousStep: () => void;
  getCurrentStep: () => TutorialStep;
}

const TutorialContext = createContext<TutorialContextType | undefined>(undefined);

export const TutorialProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isActive, setIsActive] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);

  const startTutorial = () => {
    setCurrentStep(0);
    setIsActive(true);
  };

  const endTutorial = () => {
    setIsActive(false);
    setCurrentStep(0);
  };

  const nextStep = () => {
    if (currentStep < tutorialSteps.length - 1) {
      setCurrentStep(prev => prev + 1);
    } else {
      endTutorial();
    }
  };

  const previousStep = () => {
    if (currentStep > 0) {
      setCurrentStep(prev => prev - 1);
    }
  };

  const getCurrentStep = () => tutorialSteps[currentStep];

  return (
    <TutorialContext.Provider
      value={{
        isActive,
        currentStep,
        totalSteps: tutorialSteps.length,
        startTutorial,
        endTutorial,
        nextStep,
        previousStep,
        getCurrentStep
      }}
    >
      {children}
    </TutorialContext.Provider>
  );
};

export const useTutorial = () => {
  const context = useContext(TutorialContext);
  if (!context) {
    throw new Error('useTutorial must be used within a TutorialProvider');
  }
  return context;
};