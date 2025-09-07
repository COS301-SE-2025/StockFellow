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
        ReflectionTestUtils.setField(routeConfig, "userServiceUrl", "http://user-service:4020");
        ReflectionTestUtils.setField(routeConfig, "groupServiceUrl", "http://group-service:4040");
        ReflectionTestUtils.setField(routeConfig, "transactionServiceUrl", "http://transaction-service:4080");
        ReflectionTestUtils.setField(routeConfig, "notificationServiceUrl", "http://notification-service:4050");
        ReflectionTestUtils.setField(routeConfig, "mfaServiceUrl", "http://mfa-service:8087");
    }
    
    @Test
    void shouldCreateRoutes() {
        List<Route> routes = routeConfig.routes();
        
        assertNotNull(routes);
        assertFalse(routes.isEmpty());
        assertEquals(7, routes.size()); // user register, users, groups, transaction, notifications, mfa, default api
    }
    
    @Test
    void shouldConfigureUserRegistrationRoute() {
        List<Route> routes = routeConfig.routes();
        
        Route userRegisterRoute = routes.stream()
                .filter(r -> r.getUrl().equals("/api/users/register"))
                .findFirst()
                .orElseThrow();
        
        assertFalse(userRegisterRoute.isAuth()); // No auth required for internal registration
        assertEquals("http://user-service:4020", userRegisterRoute.getProxy().getTarget());
        assertNotNull(userRegisterRoute.getRateLimit());
        assertEquals(50, userRegisterRoute.getRateLimit().getMax()); // More restrictive for registration
        assertEquals(15 * 60 * 1000L, userRegisterRoute.getRateLimit().getWindowMs());
    }
    
    @Test
    void shouldConfigureUserRoutes() {
        List<Route> routes = routeConfig.routes();
        
        Route userRoute = routes.stream()
                .filter(r -> r.getUrl().equals("/api/users/**"))
                .findFirst()
                .orElseThrow();
        
        assertTrue(userRoute.isAuth()); // Now requires auth
        assertEquals("http://user-service:4020", userRoute.getProxy().getTarget());
        assertNotNull(userRoute.getRateLimit());
        assertEquals(100, userRoute.getRateLimit().getMax()); // Updated max requests
        assertEquals(15 * 60 * 1000L, userRoute.getRateLimit().getWindowMs());
    }
    
    @Test
    void shouldConfigureGroupRoutes() {
        List<Route> routes = routeConfig.routes();
        
        Route groupRoute = routes.stream()
                .filter(r -> r.getUrl().equals("/api/groups/**"))
                .findFirst()
                .orElseThrow();
        
        assertTrue(groupRoute.isAuth()); // Requires authentication
        assertEquals("http://group-service:4040", groupRoute.getProxy().getTarget());
        assertNotNull(groupRoute.getRateLimit());
        assertEquals(100, groupRoute.getRateLimit().getMax());
        assertEquals(15 * 60 * 1000L, groupRoute.getRateLimit().getWindowMs());
    }
    
    @Test
    void shouldConfigureTransactionRouteWithAuth() {
        List<Route> routes = routeConfig.routes();
        
        Route transactionRoute = routes.stream()
                .filter(r -> r.getUrl().equals("/api/transaction/**"))
                .findFirst()
                .orElseThrow();
        
        assertTrue(transactionRoute.isAuth()); // Should require authentication
        assertEquals("http://transaction-service:4080", transactionRoute.getProxy().getTarget());
        assertNotNull(transactionRoute.getRateLimit());
        assertEquals(100, transactionRoute.getRateLimit().getMax());
        assertEquals(15 * 60 * 1000L, transactionRoute.getRateLimit().getWindowMs());
    }
    
    @Test
    void shouldConfigureNotificationRoutes() {
        List<Route> routes = routeConfig.routes();
        
        Route notificationRoute = routes.stream()
                .filter(r -> r.getUrl().equals("/api/notifications/**"))
                .findFirst()
                .orElseThrow();
        
        assertTrue(notificationRoute.isAuth()); // Requires authentication
        assertEquals("http://notification-service:4050", notificationRoute.getProxy().getTarget());
        assertNotNull(notificationRoute.getRateLimit());
        assertEquals(100, notificationRoute.getRateLimit().getMax());
        assertEquals(15 * 60 * 1000L, notificationRoute.getRateLimit().getWindowMs());
    }
    
    @Test
    void shouldConfigureMfaRoutes() {
        List<Route> routes = routeConfig.routes();
        
        Route mfaRoute = routes.stream()
                .filter(r -> r.getUrl().equals("/api/mfa/**"))
                .findFirst()
                .orElseThrow();
        
        assertFalse(mfaRoute.isAuth()); // No auth required for MFA endpoints
        assertEquals("http://mfa-service:8087", mfaRoute.getProxy().getTarget());
        assertNotNull(mfaRoute.getRateLimit());
        assertEquals(100, mfaRoute.getRateLimit().getMax());
        assertEquals(15 * 60 * 1000L, mfaRoute.getRateLimit().getWindowMs());
    }
    
    @Test
    void shouldConfigureDefaultApiRoute() {
        List<Route> routes = routeConfig.routes();
        
        Route defaultRoute = routes.stream()
                .filter(r -> r.getUrl().equals("/api"))
                .findFirst()
                .orElseThrow();
        
        assertFalse(defaultRoute.isAuth()); // No auth required for default route
        assertEquals("http://user-service:4020", defaultRoute.getProxy().getTarget());
        assertNotNull(defaultRoute.getRateLimit());
        assertEquals(100, defaultRoute.getRateLimit().getMax());
        assertEquals(15 * 60 * 1000L, defaultRoute.getRateLimit().getWindowMs());
    }
    
    @Test
    void shouldConfigureRateLimitingConsistently() {
        List<Route> routes = routeConfig.routes();
    
        routes.forEach(route -> {
            assertNotNull(route.getRateLimit());
            assertEquals(15 * 60 * 1000L, route.getRateLimit().getWindowMs()); // 15 minutes for all routes
            
            // Check specific max values based on route
            if (route.getUrl().equals("/api/users/register")) {
                assertEquals(50, route.getRateLimit().getMax()); // More restrictive for registration
            } else {
                assertEquals(100, route.getRateLimit().getMax()); // Standard limit for other routes
            }
        });
    }
}