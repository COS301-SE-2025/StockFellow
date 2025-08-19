// DemoController.java
package com.stockfellow.demoservice.controller;

import com.stockfellow.demoservice.dto.DemoRequest;
import com.stockfellow.demoservice.orchestrator.DemoOrchestrator;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoController {
    
    @Autowired
    private DemoOrchestrator demoOrchestrator;
    
    // API endpoint for programmatic access
    @PostMapping("/start")
    public ResponseEntity<DemoRequest> startDemo(@RequestBody String scenarioType) {
        try {
            DemoRequest response = new DemoRequest();
            response.setScenarioType(scenarioType);
            response.setSessionId("1");
            String message = demoOrchestrator.startDemo(response);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            DemoRequest response = new DemoRequest();
            response.setMessage("Demo failed: " + e.getMessage());
            response.setScenarioType(scenarioType);
            response.setSessionId("1");
            return ResponseEntity.status(500)
                .body(response);
        }
    }
    
    // // Get demo status/progress
    // @GetMapping("/status/{sessionId}")
    // public ResponseEntity<DemoStatus> getDemoStatus(@PathVariable String sessionId) {
    //     DemoStatus status = demoOrchestrator.getDemoStatus(sessionId);
    //     return ResponseEntity.ok(status);
    // }
    
    // // Get available demo scenarios
    // @GetMapping("/scenarios")
    // public ResponseEntity<List<DemoScenario>> getAvailableScenarios() {
    //     return ResponseEntity.ok(demoOrchestrator.getAvailableScenarios());
    // }
    
    // Cleanup demo data
    @DeleteMapping("/cleanup/{sessionId}")
    public ResponseEntity<String> cleanupDemo(@PathVariable String sessionId) {
        demoOrchestrator.cleanup(sessionId);
        return ResponseEntity.ok("Demo data cleaned up successfully");
    }
}