// src/components/StokvelMandate.tsx
import React, { useState } from "react";
import { View, Text, Modal, TouchableOpacity, ScrollView } from "react-native";
import SimpleRadioBox from "./SimpleRadioBox";

interface StokvelMandateProps {
    visible: boolean;
    onClose: () => void;
    onAccept: () => void;
}

const StokvelMandate = ({ visible, onClose, onAccept }: StokvelMandateProps) => {
    const [accepted, setAccepted] = useState(false);

    const handleAccept = () => {
        if (accepted) {
            onAccept();
        }
        onClose();
    };

    return (
        <Modal
            animationType="slide"
            transparent={true}
            visible={visible}
            onRequestClose={onClose}
        >
            <View className="flex-1 justify-center items-center bg-black/50">
                <View className="w-11/12 bg-white rounded-2xl p-7 max-h-[80%]">
                    <Text className="text-xl font-['PlusJakartaSans-Bold'] mb-4 text-center">
                        Stokvel Membership Terms
                    </Text>

                    <ScrollView className="mb-4">
                        <Text className="text-sm font-['PlusJakartaSans-Regular'] mb-3">
                            By joining this stokvel, you agree to the following terms and conditions:
                        </Text>

                        <Text className="text-sm font-['PlusJakartaSans-Regular'] mb-3">
                            1. You commit to making regular contributions as determined by the stokvel rules.
                        </Text>

                        <Text className="text-sm font-['PlusJakartaSans-Regular'] mb-3">
                            2. Failure to make contributions may result in penalties or removal from the stokvel.
                        </Text>

                        <Text className="text-sm font-['PlusJakartaSans-Regular'] mb-3">
                            3. Payouts and contributions will be made according to the stokvel's established rotation schedule.
                        </Text>

                        <Text className="text-sm font-['PlusJakartaSans-Regular'] mb-3">
                            4. The amount paid to you in a payout cycle can only equal the amount contributed by you in the same payout cycle.
                        </Text>

                        <Text className="text-sm font-['PlusJakartaSans-Regular'] mb-4">
                            5. The stokvel administrators reserve the right to approve or reject membership requests.
                        </Text>

                        <View className="flex-row items-center my-3">
                            <SimpleRadioBox
                                isSelected={accepted}
                                onPress={() => setAccepted(!accepted)}
                            />
                            <Text className="ml-2 text-sm font-['PlusJakartaSans-Regular']">
                                I accept the terms and conditions
                            </Text>
                        </View>
                    </ScrollView>

                    <View className="flex-row justify-between">
                        <TouchableOpacity
                            className="px-4 py-3"
                            onPress={onClose}
                        >
                            <Text className="text-base font-['PlusJakartaSans-SemiBold']">
                                Cancel
                            </Text>
                        </TouchableOpacity>

                        <TouchableOpacity
                            className={`px-4 py-3 rounded-3xl ${accepted ? "bg-[#0C0C0F]" : "bg-gray-300"}`}
                            onPress={handleAccept}
                            disabled={!accepted}
                        >
                            <Text className="text-base font-['PlusJakartaSans-SemiBold'] text-white">
                                Submit Request
                            </Text>
                        </TouchableOpacity>
                    </View>
                </View>
            </View>
        </Modal>
    );
};

export default StokvelMandate;