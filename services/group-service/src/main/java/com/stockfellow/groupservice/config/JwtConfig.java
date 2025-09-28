package com.stockfellow.groupservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class JwtConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll() // Health check endpoints
                .anyRequest().authenticated()
            )
            .addFilterBefore(new HeaderAuthFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Simple filter that trusts the API Gateway's authentication
     * The gateway validates JWT tokens and passes user information via headers
     */
    public class HeaderAuthFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            
            // Check if request came through API Gateway (has forwarded headers)
            String forwardedFor = request.getHeader("X-Forwarded-For");
            String forwardedProto = request.getHeader("X-Forwarded-Proto");
            
            if (forwardedFor != null || forwardedProto != null) {
                // Request came through API Gateway - trust it
                // Extract user ID from Authorization header if present (for logging/context)
                String authHeader = request.getHeader("Authorization");
                String userId = "anonymous";
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    // You could optionally decode the JWT here just to extract user ID
                    // without validating it (since gateway already did that)
                    userId = extractUserIdFromToken(authHeader.substring(7));
                }
                
                // Create authenticated user context
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER")
                );
                
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                );
                
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            // If no forwarded headers, request might be direct - let it through for health checks
            
            filterChain.doFilter(request, response);
        }
        
        private String extractUserIdFromToken(String token) {
            try {
                // Just decode without verification (gateway already verified)
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    // Simple extraction - you could use a JSON library here
                    if (payload.contains("\"sub\":")) {
                        int start = payload.indexOf("\"sub\":\"") + 7;
                        int end = payload.indexOf("\"", start);
                        return payload.substring(start, end);
                    }
                }
            } catch (Exception e) {
                // If we can't extract user ID, use anonymous
                System.err.println("Could not extract user ID from token: " + e.getMessage());
            }
            return "anonymous";
        }
    }
}