package com.stockfellow.groupservice.dto;

import java.util.Date;

public  class NextPayeeResult {
        private final String groupId;
        private final String groupName;
        private final String recipientId;
        private final String recipientUsername;
        private final String recipientRole;
        private final Integer currentPosition;
        private final Integer totalMembers;
        private final Double groupBalance;
        private final String lastPayoutRecipient;
        private final Date lastPayoutDate;
        private final String payoutFrequency;
        private final Date nextPayoutDate;

        public NextPayeeResult(String groupId, String groupName, String recipientId, String recipientUsername,
                              String recipientRole, Integer currentPosition, Integer totalMembers, 
                              Double groupBalance, String lastPayoutRecipient, Date lastPayoutDate,
                              String payoutFrequency, Date nextPayoutDate) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.recipientId = recipientId;
            this.recipientUsername = recipientUsername;
            this.recipientRole = recipientRole;
            this.currentPosition = currentPosition;
            this.totalMembers = totalMembers;
            this.groupBalance = groupBalance;
            this.lastPayoutRecipient = lastPayoutRecipient;
            this.lastPayoutDate = lastPayoutDate;
            this.payoutFrequency = payoutFrequency;
            this.nextPayoutDate = nextPayoutDate;
        }

        // Getters
        public String getGroupId() { return groupId; }
        public String getGroupName() { return groupName; }
        public String getRecipientId() { return recipientId; }
        public String getRecipientUsername() { return recipientUsername; }
        public String getRecipientRole() { return recipientRole; }
        public Integer getCurrentPosition() { return currentPosition; }
        public Integer getTotalMembers() { return totalMembers; }
        public Double getGroupBalance() { return groupBalance; }
        public String getLastPayoutRecipient() { return lastPayoutRecipient; }
        public Date getLastPayoutDate() { return lastPayoutDate; }
        public String getPayoutFrequency() { return payoutFrequency; }
        public Date getNextPayoutDate() { return nextPayoutDate; }
    }
