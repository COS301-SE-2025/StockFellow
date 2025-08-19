package com.stockfellow.userservice.config;

package com.stockfellow.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API endpoints
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Allow unauthenticated access to these endpoints
                .requestMatchers("/api/users").permitAll() // Service info endpoint
                .requestMatchers("/api/users/register").permitAll() // Registration from Gateway
                .requestMatchers("/actuator/**").permitAll() // Health checks and actuator endpoints
                .requestMatchers("/health").permitAll() // Health check
                .requestMatchers("/info").permitAll() // Info endpoint
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
