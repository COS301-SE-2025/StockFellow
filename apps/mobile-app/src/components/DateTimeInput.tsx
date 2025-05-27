import React, { useState } from 'react';
import { View, TextInput, TouchableOpacity, Text, Image } from 'react-native';
import DateTimePicker from '@react-native-community/datetimepicker';
import { icons } from "../constants";

interface DateTimeField {
  date: Date | null;
  time: Date | null;
  datetime: Date | null;
  showDatePicker: boolean;
  showTimePicker: boolean;
}

const DateTimeInput: React.FC = () => {
  const [fields, setFields] = useState<DateTimeField[]>([
    { date: null, time: null, datetime: null, showDatePicker: false, showTimePicker: false },
  ]);

  const handleDateChange = (index: number, event: any, selectedDate?: Date) => {
    const updatedFields = [...fields];
    updatedFields[index].showDatePicker = false;
    if (selectedDate) {
      updatedFields[index].date = selectedDate;
      updatedFields[index].datetime = mergeDateTime(selectedDate, updatedFields[index].time);
    }
    setFields(updatedFields);
  };

  const handleTimeChange = (index: number, event: any, selectedTime?: Date) => {
    const updatedFields = [...fields];
    updatedFields[index].showTimePicker = false;
    if (selectedTime) {
      updatedFields[index].time = selectedTime;
      updatedFields[index].datetime = mergeDateTime(updatedFields[index].date, selectedTime);
    }
    setFields(updatedFields);
  };

  const mergeDateTime = (date: Date | null, time: Date | null) => {
    if (!date || !time) return null;
    const merged = new Date(date);
    merged.setHours(time.getHours(), time.getMinutes());
    return merged;
  };

  

  return (
    <View>
      <Text className="text-base">Collection Dates</Text>
      {fields.map((field, index) => (
        <View key={index} className="flex-row justify-between p-3 border border-gray-200 rounded-xl bg-gray-50 mt-3">
          <View className="flex-row items-center w-[48%] border border-gray-300 rounded-lg px-3 py-2">
            <TextInput
              className="flex-1 text-gray-800"
              placeholder="Select date"
              value={field.date?.toLocaleDateString() || ''}
              editable={false}
            />
            <TouchableOpacity onPress={() => {
              const updatedFields = [...fields];
              updatedFields[index].showDatePicker = true;
              setFields(updatedFields);
            }}>
              <Image source={icons.calendar} className="w-6 h-6 tint-accent" />
            </TouchableOpacity>
            {field.showDatePicker && (
              <DateTimePicker
                value={field.date || new Date()}
                mode="date"
                display="default"
                onChange={(event, date) => handleDateChange(index, event, date)}
              />
            )}
          </View>

          <View className="flex-row items-center w-[48%] border border-gray-300 rounded-lg px-3 py-2">
            <TextInput
              className="flex-1 text-gray-800"
              placeholder="Select time"
              value={field.time?.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) || ''}
              editable={false}
            />
            <TouchableOpacity onPress={() => {
              const updatedFields = [...fields];
              updatedFields[index].showTimePicker = true;
              setFields(updatedFields);
            }}>
              <Image source={icons.clock} className="w-6 h-6 tint-accent" />
            </TouchableOpacity>
            {field.showTimePicker && (
              <DateTimePicker
                value={field.time || new Date()}
                mode="time"
                display="default"
                onChange={(event, time) => handleTimeChange(index, event, time)}
              />
            )}
          </View>
        </View>
      ))}
    </View>
  );
};

export default DateTimeInput;