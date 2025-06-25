package com.stockfellow.gateway.config;

import com.stockfellow.gateway.model.Route;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RouteConfig {
    
    @Value("${services.user-service.url:http://user-service:4000}")
    private String userServiceUrl;
    
    @Value("${services.group-service.url:http://group-service:4040}")
    private String groupServiceUrl;
    
    @Value("${services.transaction-service.url:http://transaction-service:4080}")
    private String transactionServiceUrl;
    
    @Bean
    public List<Route> routes() {
        return Arrays.asList(
            // User service route
            new Route(
                "/api/users/**",
                false,
                new Route.RateLimit(15 * 60 * 1000L, 10),
                new Route.Proxy(userServiceUrl, true)
            ),
            
            // Group service route
            new Route(
                "/api/group/**",
                false,
                new Route.RateLimit(15 * 60 * 1000L, 10),
                new Route.Proxy(groupServiceUrl, true)
            ),
            
            // Transaction service route (requires auth)
            new Route(
                "/api/transaction/**",
                true,
                new Route.RateLimit(15 * 60 * 1000L, 10),
                new Route.Proxy(transactionServiceUrl, true)
            ),
            
            // Default api route
            new Route(
                "/api",
                false,
                new Route.RateLimit(15 * 60 * 1000L, 10),
                new Route.Proxy(userServiceUrl, true)
            )
        );
    }
}