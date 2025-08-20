package com.stockfellow.demoservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.stockfellow.demoservice.model.*;
import java.util.Map;

@Component
public class UserServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.user.url}")
    private String userServiceUrl;
    
    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public User createUser(String email, String name, String password) {
        // Implementation for calling user service
        return webClient.post()
            .uri(userServiceUrl + "/users")
            .bodyValue(Map.of("email", email, "name", name, "password", password))
            .retrieve()
            .bodyToMono(User.class)
            .block();
    }
    
    public void deleteUser(String userId) {
        webClient.delete()
            .uri(userServiceUrl + "/users/" + userId)
            .retrieve()
            .toBodilessEntity()
            .block();
    }
}
