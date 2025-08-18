package com.stockfellow.gateway.unit.config;

import com.stockfellow.gateway.config.RouteConfig;
import com.stockfellow.gateway.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteConfigTest {
    
    private RouteConfig routeConfig;
    
    @BeforeEach
    void setUp() {
        routeConfig = new RouteConfig();
        ReflectionTestUtils.setField(routeConfig, "userServiceUrl", "http://user-service:4000");
        ReflectionTestUtils.setField(routeConfig, "groupServiceUrl", "http://group-service:4040");
        // ReflectionTestUtils.setField(routeConfig, "transactionServiceUrl", "http://transaction-service:4080");
    }
    
    @Test
    void shouldCreateRoutes() {
        List<Route> routes = routeConfig.routes();
        
        assertNotNull(routes);
        assertFalse(routes.isEmpty());
        assertEquals(4, routes.size()); // user, users, group, transaction, default api
    }
    
    @Test
    void shouldConfigureUserRoutes() {
        List<Route> routes = routeConfig.routes();
        
        Route userRoute = routes.stream()
                .filter(r -> r.getUrl().equals("/api/users/**"))
                .findFirst()
                .orElseThrow();
        
        assertFalse(userRoute.isAuth());
        assertEquals("http://user-service:4000", userRoute.getProxy().getTarget());
        assertNotNull(userRoute.getRateLimit());
        assertEquals(10, userRoute.getRateLimit().getMax());
    }
    
    // @Test
    // void shouldConfigureTransactionRouteWithAuth() {
    //     List<Route> routes = routeConfig.routes();
        
    //     Route transactionRoute = routes.stream()
    //             .filter(r -> r.getUrl().equals("/api/transaction"))
    //             .findFirst()
    //             .orElseThrow();
        
    //     assertTrue(transactionRoute.isAuth()); // Should require authentication
    //     assertEquals("http://transaction-service:4080", transactionRoute.getProxy().getTarget());
    // }
    
    @Test
    void shouldConfigureRateLimiting() {
        List<Route> routes = routeConfig.routes();
    
        routes.forEach(route -> {
            assertNotNull(route.getRateLimit());
            assertEquals(15 * 60 * 1000L, route.getRateLimit().getWindowMs()); // 15 minutes
            assertEquals(10, route.getRateLimit().getMax()); // 10 requests per window
        });
    }
}