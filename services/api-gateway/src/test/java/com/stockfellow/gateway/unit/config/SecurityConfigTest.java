package com.stockfellow.gateway.unit.config;

import com.stockfellow.gateway.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {
    
    private SecurityConfig securityConfig;
    
    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }
    
    @Test
    void shouldCreateSimpleAuthorityMapper() {
        // When
        SimpleAuthorityMapper mapper = securityConfig.authoritiesMapper();
        
        // Then
        assertNotNull(mapper);
        assertInstanceOf(SimpleAuthorityMapper.class, mapper);
    }
    
    @Test
    void shouldHaveCorrectClassStructure() {
        // Verify class has KeycloakConfiguration annotation
        assertTrue(securityConfig.getClass().isAnnotationPresent(
            org.keycloak.adapters.springsecurity.KeycloakConfiguration.class));
        
        // Verify it extends the correct base class
        assertEquals("KeycloakWebSecurityConfigurerAdapter", 
            securityConfig.getClass().getSuperclass().getSimpleName());
    }
    
    @Test
    void shouldHaveAuthorityMapperBean() {
        // Test that the authoritiesMapper method returns a valid mapper
        SimpleAuthorityMapper mapper = securityConfig.authoritiesMapper();
        
        assertNotNull(mapper);
        assertInstanceOf(SimpleAuthorityMapper.class, mapper);
    }
    
    @Test
    void shouldBeProperlyAnnotated() {
        // Check that the class has the correct Spring Security configuration
        Class<?> configClass = securityConfig.getClass();
        
        // Should have KeycloakConfiguration annotation
        assertNotNull(configClass.getAnnotation(
            org.keycloak.adapters.springsecurity.KeycloakConfiguration.class));
        
        // Should extend KeycloakWebSecurityConfigurerAdapter
        assertTrue(configClass.getSuperclass().getSimpleName()
            .contains("KeycloakWebSecurityConfigurerAdapter"));
    }
    
    @Test
    void shouldConfigureAuthorityMapperCorrectly() {
        // When
        SimpleAuthorityMapper mapper = securityConfig.authoritiesMapper();
        
        // Then
        assertNotNull(mapper);
        
        // Test that it's a SimpleAuthorityMapper instance
        assertEquals("SimpleAuthorityMapper", mapper.getClass().getSimpleName());
    }
    
    @Test
    void shouldHaveConfigurationMethods() {
        // Verify that the class has the expected configuration methods
        Class<?> configClass = securityConfig.getClass();
        
        // Check for authoritiesMapper method
        assertDoesNotThrow(() -> {
            configClass.getMethod("authoritiesMapper");
        });
        
        // Check for configureGlobal method
        assertDoesNotThrow(() -> {
            configClass.getMethod("configureGlobal", 
                org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder.class);
        });
    }
}