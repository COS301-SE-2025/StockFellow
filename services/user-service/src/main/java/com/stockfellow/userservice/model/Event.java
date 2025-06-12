package main.java.com.stockfellow.userservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Document(collection = "events")
public class Event {
    @Id
    private String id;
    private String eventType;
    private Map<String, Object> data;
    private Date timestamp;

    public Event() {
        this.timestamp = new Date();
    }

    public Event(String eventType, Map<String, Object> data) {
        this.eventType = eventType;
        this.data = data;
        this.timestamp = new Date();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}