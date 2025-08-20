package com.stockfellow.demoservice.model;
import java.util.List;

public class Group {
    private String id;
    private String name;
    private String description;
    private List<String> userIds;
    
    // Constructors, getters, setters
    public Group() {}
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getUserIds() { return userIds; }
    public void setUserIds(List<String> userIds) { this.userIds = userIds; }
}