package com.stockfellow.userservice.service;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class SouthAfricanIdValidationService {
    
    private static final Pattern SA_ID_PATTERN = Pattern.compile("^\\d{13}$");
    
    /**
     * Validates a South African ID number using the Luhn algorithm
     * @param idNumber The 13-digit SA ID number as a string
     * @return true if valid, false otherwise
     */
    public boolean validateSouthAfricanId(String idNumber) {
        if (idNumber == null || !SA_ID_PATTERN.matcher(idNumber).matches()) {
            return false;
        }
        
        // Validate date of birth part (first 6 digits)
        if (!isValidDateOfBirth(idNumber.substring(0, 6))) {
            return false;
        }
        
        // Validate citizenship digit (10th digit should be 0 or 1)
        char citizenshipDigit = idNumber.charAt(10);
        if (citizenshipDigit != '0' && citizenshipDigit != '1') {
            return false;
        }
        
        // Apply Luhn algorithm for checksum validation
        return isValidLuhnChecksum(idNumber);
    }
    
    /**
     * Validates the date of birth portion of the SA ID
     * Format: YYMMDD
     */
    private boolean isValidDateOfBirth(String dobPart) {
        try {
            int year = Integer.parseInt(dobPart.substring(0, 2));
            int month = Integer.parseInt(dobPart.substring(2, 4));
            int day = Integer.parseInt(dobPart.substring(4, 6));
            
            // Basic month validation
            if (month < 1 || month > 12) {
                return false;
            }
            
            // Basic day validation
            if (day < 1 || day > 31) {
                return false;
            }
            
            // More specific day validation for certain months
            if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
                return false;
            }
            
            // February validation (not accounting for leap years in this simple check)
            if (month == 2 && day > 29) {
                return false;
            }
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Applies the Luhn algorithm to validate the checksum
     */
    private boolean isValidLuhnChecksum(String idNumber) {
        int sum = 0;
        boolean alternate = false;
        
        // Process digits from right to left (excluding the last digit which is the check digit)
        for (int i = idNumber.length() - 2; i >= 0; i--) {
            int digit = Character.getNumericValue(idNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        // Calculate the check digit
        int checkDigit = (10 - (sum % 10)) % 10;
        int actualCheckDigit = Character.getNumericValue(idNumber.charAt(idNumber.length() - 1));
        
        return checkDigit == actualCheckDigit;
    }
    
    /**
     * Extracts additional information from a valid SA ID number
     */
    public SouthAfricanIdInfo extractIdInfo(String idNumber) {
        if (!validateSouthAfricanId(idNumber)) {
            return null;
        }
        
        SouthAfricanIdInfo info = new SouthAfricanIdInfo();
        info.setIdNumber(idNumber);
        
        // Extract date of birth
        String dobPart = idNumber.substring(0, 6);
        int year = Integer.parseInt(dobPart.substring(0, 2));
        int month = Integer.parseInt(dobPart.substring(2, 4));
        int day = Integer.parseInt(dobPart.substring(4, 6));
        
        // Determine century (00-21 = 2000s, 22-99 = 1900s)
        int fullYear = (year <= 21) ? 2000 + year : 1900 + year;
        
        info.setDateOfBirth(String.format("%04d-%02d-%02d", fullYear, month, day));
        
        // Extract gender (7th digit: 0-4 = female, 5-9 = male)
        int genderDigit = Character.getNumericValue(idNumber.charAt(6));
        info.setGender(genderDigit < 5 ? "Female" : "Male");
        
        // Extract citizenship (11th digit: 0 = SA citizen, 1 = permanent resident)
        int citizenshipDigit = Character.getNumericValue(idNumber.charAt(10));
        info.setCitizenship(citizenshipDigit == 0 ? "South African Citizen" : "Permanent Resident");
        
        return info;
    }
    
    /**
     * Data class to hold extracted SA ID information
     */
    public static class SouthAfricanIdInfo {
        private String idNumber;
        private String dateOfBirth;
        private String gender;
        private String citizenship;
        
        // Getters and setters
        public String getIdNumber() { return idNumber; }
        public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
        
        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        
        public String getCitizenship() { return citizenship; }
        public void setCitizenship(String citizenship) { this.citizenship = citizenship; }
        
        @Override
        public String toString() {
            return "SouthAfricanIdInfo{" +
                    "idNumber='" + idNumber + '\'' +
                    ", dateOfBirth='" + dateOfBirth + '\'' +
                    ", gender='" + gender + '\'' +
                    ", citizenship='" + citizenship + '\'' +
                    '}';
        }
    }
}
