package com.stockfellow.demoservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker options
     * This sets up the message routing for WebSocket communications
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry the greeting messages 
        // back to the client on destinations prefixed with "/topic"
        config.enableSimpleBroker("/topic", "/queue");
        
        // Define that messages whose destination begins with "/app" 
        // should be routed to message-handling methods
        config.setApplicationDestinationPrefixes("/app");
        
        // Optional: Set user destination prefix (for user-specific messages)
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints
     * This defines the WebSocket connection endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint for WebSocket connections
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Allow all origins for development
                .withSockJS();  // Enable SockJS fallback options
        
        // Optional: Add a plain WebSocket endpoint (without SockJS)
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("*");
    }
}