package com.stockfellow.groupservice.dto;

public class CreateGroupResult {
    private final String groupId;
    private final String eventId;
    private final String message;

    public CreateGroupResult(String groupId, String eventId, String message) {
        this.groupId = groupId;
        this.eventId = eventId;
        this.message = message;
    }

    public String getGroupId() { return groupId; }
    public String getEventId() { return eventId; }
    public String getMessage() { return message; }
}