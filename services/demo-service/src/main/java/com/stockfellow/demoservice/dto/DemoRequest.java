package com.stockfellow.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class DemoRequest {
    @NotNull
    @JsonProperty("sessionId")
    private String sessionId;
    
    @NotBlank
    @JsonProperty("scenarioType")
    private String scenarioType;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("parameters")
    private Map<String, Object> parameters;
    
    @JsonProperty("status")
    private String status;
    
    
    // Constructors, getters, setters
    public DemoRequest() {
        this.status = "STARTED";
    }
    
    public String getScenarioType() { return scenarioType; }
    public void setScenarioType(String scenarioType) { this.scenarioType = scenarioType; }
    
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
