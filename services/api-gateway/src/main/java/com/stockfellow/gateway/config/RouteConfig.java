package com.stockfellow.gateway.config;

import com.stockfellow.gateway.model.Route;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RouteConfig {

    // @Value("${services.user-service.url:http://user-service:4020}")
    // private String userServiceUrl;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.group-service.url}")
    private String groupServiceUrl;

    @Value("${services.transaction-service.url}")
    private String transactionServiceUrl;

    @Value("${services.notification-service.url}")
    private String notificationServiceUrl;

    @Value("${services.mfa-service.url}")
    private String mfaServiceUrl;

    @Bean
    public List<Route> routes() {
        return Arrays.asList(
                // User service route
                new Route(
                        "/api/users/register", // Registration endpoint (internal use by gateway)
                        false, // No auth required as it's called internally
                        new Route.RateLimit(15 * 60 * 1000L, 50), // More restrictive for registration
                        new Route.Proxy(userServiceUrl, true)),

                new Route(
                        "/api/users/**",
                        true,
                        new Route.RateLimit(15 * 60 * 1000L, 100),
                        new Route.Proxy(userServiceUrl, true)),

                // Group service route
                new Route(
                        "/api/groups/**",
                        true,
                        new Route.RateLimit(15 * 60 * 1000L, 100),
                        new Route.Proxy(groupServiceUrl, true)),

                // Transaction service route (requires auth)
                new Route(
                        "/api/transaction/**",
                        true,
                        new Route.RateLimit(15 * 60 * 1000L, 100),
                        new Route.Proxy(transactionServiceUrl, true)),

                // Notification service route
                new Route(
                        "/api/notifications/**",
                        true,
                        new Route.RateLimit(15 * 60 * 1000L, 100),
                        new Route.Proxy(notificationServiceUrl, true)),

                // MFA routes
                new Route(
                        "/api/mfa/**",
                        false,
                        new Route.RateLimit(15 * 60 * 1000L, 100),
                        new Route.Proxy(mfaServiceUrl, true)),

                // Default api route
                new Route(
                        "/api",
                        false,
                        new Route.RateLimit(15 * 60 * 1000L, 100),
                        new Route.Proxy(userServiceUrl, true)));
    }
}