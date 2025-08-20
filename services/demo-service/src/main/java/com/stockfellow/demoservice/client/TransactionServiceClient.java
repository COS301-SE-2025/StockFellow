package com.stockfellow.demoservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.stockfellow.demoservice.model.*;

@Component
public class TransactionServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.transaction.url}")
    private String transactionServiceUrl;
    
    public TransactionServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    // Add methods for transaction operations
}
