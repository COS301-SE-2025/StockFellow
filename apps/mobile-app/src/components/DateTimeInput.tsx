import React, { useState } from 'react';
import { View, TextInput, TouchableOpacity, Text, Image } from 'react-native';
import DateTimePicker from '@react-native-community/datetimepicker';
import { icons } from "../constants";

interface DateTimeInputProps {
  label?: string;
  onDateTimeChange?: (datetime: Date | null) => void;
}

const DateTimeInput: React.FC<DateTimeInputProps> = ({ label = "Collection Date", onDateTimeChange }) => {
  const [date, setDate] = useState<Date | null>(null);
  const [time, setTime] = useState<Date | null>(null);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);

  const handleDateChange = (event: any, selectedDate?: Date) => {
    setShowDatePicker(false);
    if (selectedDate) {
      setDate(selectedDate);
      if (time) {
        const merged = new Date(selectedDate);
        merged.setHours(time.getHours(), time.getMinutes());
        onDateTimeChange?.(merged);
      }
    }
  };

  const handleTimeChange = (event: any, selectedTime?: Date) => {
    setShowTimePicker(false);
    if (selectedTime) {
      setTime(selectedTime);
      if (date) {
        const merged = new Date(date);
        merged.setHours(selectedTime.getHours(), selectedTime.getMinutes());
        onDateTimeChange?.(merged);
      }
    }
  };

  return (
    <View className="mb-5">
      <Text className="text-sm font-light mb-2">{label}</Text>
      <View className="flex-row justify-between">
        <TouchableOpacity 
          className="flex-1 border border-gray-300 rounded-lg px-3 py-3 mr-2"
          onPress={() => setShowDatePicker(true)}
        >
          <View className="flex-row justify-between items-center">
            <Text className={` ${date ? 'text-gray-800' : 'text-[#C5C6CC] font-light'}`}>
              {date ? date.toLocaleDateString() : "Select date"}
            </Text>
            <Image source={icons.calendar} className="w-6 h-6 tint-accent" />
          </View>
        </TouchableOpacity>

        <TouchableOpacity 
          className="flex-1 border border-gray-300 rounded-lg px-3 py-3 ml-2"
          onPress={() => setShowTimePicker(true)}
        >
          <View className="flex-row justify-between items-center">
            <Text className={`${time ? 'text-gray-800' : 'text-gray-400'}`}>
              {time ? time.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : "Select time"}
            </Text>
            <Image source={icons.clock} className="w-6 h-6 tint-accent" />
          </View>
        </TouchableOpacity>

        {showDatePicker && (
          <DateTimePicker
            value={date || new Date()}
            mode="date"
            display="default"
            onChange={handleDateChange}
          />
        )}

        {showTimePicker && (
          <DateTimePicker
            value={time || new Date()}
            mode="time"
            display="default"
            onChange={handleTimeChange}
          />
        )}
      </View>
    </View>
  );
};

export default DateTimeInput;