import React, { useEffect, useState } from 'react';
import { Modal, View, Text, TouchableOpacity, ActivityIndicator, Image } from 'react-native';
import { useTheme } from '../../app/_layout';
import userService from '../services/userService';
import { icons } from '../constants';

interface TierModalProps {
  isVisible: boolean;
  onClose: () => void;
}

const TierModal: React.FC<TierModalProps> = ({ isVisible, onClose }) => {
  const { colors } = useTheme();
  const [loading, setLoading] = useState(false);
  const [tierInfo, setTierInfo] = useState<{
    tier?: number;
    tierName?: string;
    confidence?: number;
    contributionRange?: { min: number; max: number };
  } | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isVisible) return;
    let mounted = true;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await userService.getUserAffordabilityTier();
        if (!mounted) return;
        setTierInfo(res || null);
      } catch (err) {
        setError('Failed to load tier');
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, [isVisible]);

  return (
    <Modal
      visible={isVisible}
      animationType="slide"
      transparent
      onRequestClose={onClose}
    >
      <View style={{ flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(0,0,0,0.35)' }}>
        <View style={{
          backgroundColor: colors.card,
          borderTopLeftRadius: 20,
          borderTopRightRadius: 20,
          padding: 20,
          minHeight: 220,
        }}>
          {/* Header */}
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <Text style={{ color: colors.text, fontSize: 18, fontWeight: '700' }}>Your Tier</Text>
            <TouchableOpacity onPress={onClose} className="p-2">
              <Image
                source={icons.close}
                style={{ width: 18, height: 18, tintColor: colors.text }}
                resizeMode="contain"
              />
            </TouchableOpacity>
          </View>

          {loading ? (
            <View style={{ alignItems: 'center', paddingVertical: 20 }}>
              <ActivityIndicator size="small" color={colors.primary} />
            </View>
          ) : error ? (
            <Text style={{ color: colors.text, opacity: 0.7 }}>{error}</Text>
          ) : tierInfo ? (
            <View>
              <Text style={{ color: colors.text, fontSize: 16, fontWeight: '700', marginBottom: 6 }}>
                {tierInfo.tierName || `Tier ${tierInfo.tier || 'Unanalyzed'}`}
              </Text>
              {tierInfo.contributionRange && (
                <Text style={{ color: colors.text, opacity: 0.8, marginBottom: 6 }}>
                  Suggested contribution: R{tierInfo.contributionRange.min} - R{tierInfo.contributionRange.max}
                </Text>
              )}
              {typeof tierInfo.confidence === 'number' && (
                <Text style={{ color: colors.text, opacity: 0.7 }}>
                  Confidence: {Math.round((tierInfo.confidence || 0) * 100) / 100}%
                </Text>
              )}
            </View>
          ) : (
            <Text style={{ color: colors.text, opacity: 0.7 }}>No tier information available.</Text>
          )}

          {/* Close Button (matching other modals) */}
          <View style={{ marginTop: 18 }}>
            <TouchableOpacity
              onPress={onClose}
              className="bg-[#1DA1FA] rounded-xl py-3"
            >
              <Text className="text-center text-white font-['PlusJakartaSans-SemiBold']">
                Got it
              </Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

export default TierModal;