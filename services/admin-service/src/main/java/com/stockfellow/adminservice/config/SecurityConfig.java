package com.stockfellow.adminservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult; 
import org.springframework.security.oauth2.core.OAuth2Error; 
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/admin/auth/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt
                            .jwtAuthenticationConverter(jwtAuthenticationConverter())
                            .decoder(jwtDecoder())
                    ));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        logger.debug("Configuring JwtDecoder with custom JWK Set URI");
        
        String jwkSetUri = "https://134.122.73.157.nip.io/realms/stockfellow/protocol/openid-connect/certs";
        logger.info("JWK Set URI: {}", jwkSetUri);
        
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();
        
        // Use the default validators but with flexible issuer validation
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        
        OAuth2TokenValidator<Jwt> customIssuerValidator = new OAuth2TokenValidator<Jwt>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt jwt) {
                String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
                logger.debug("Validating token with issuer: {}", issuer);
                
                // Accept both keycloak (internal) and localhost (external) as valid issuers
                if (issuer.equals("https://134.122.73.157.nip.io/realms/stockfellow") || 
                    issuer.equals("http://localhost:8080/realms/stockfellow")) {
                    logger.debug("Issuer validation passed for: {}", issuer);
                    return OAuth2TokenValidatorResult.success();
                }
                
                logger.warn("Invalid issuer: {}", issuer);
                OAuth2Error error = new OAuth2Error("invalid_issuer", 
                    "Token issuer is not valid: " + issuer, null);
                return OAuth2TokenValidatorResult.failure(error);
            }
        };
        
        // Combine default validation with custom issuer validation
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
            defaultValidator, customIssuerValidator);
        
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        logger.debug("Configuring JwtAuthenticationConverter");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            logger.debug("Processing JWT claims for authorities");
            logger.debug("JWT subject: {}", jwt.getSubject());
            logger.debug("JWT issuer: {}", jwt.getIssuer());
            
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            
            // Extract realm roles from realm_access.roles
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                logger.debug("Found realm roles: {}", roles);
                
                for (String role : roles) {
                    String authority = "ROLE_" + role;
                    authorities.add(new SimpleGrantedAuthority(authority));
                    logger.debug("Added authority: {}", authority);
                }
            } else {
                logger.warn("No realm_access found in JWT claims");
            }
            
            // Also check resource_access for client-specific roles
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                logger.debug("Resource access found for clients: {}", resourceAccess.keySet());
                
                // Check for admin-service-client specific roles
                @SuppressWarnings("unchecked")
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("admin-service-client");
                if (clientAccess != null && clientAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> clientRoles = (List<String>) clientAccess.get("roles");
                    logger.debug("Found client roles: {}", clientRoles);
                    
                    for (String role : clientRoles) {
                        String authority = "ROLE_" + role;
                        authorities.add(new SimpleGrantedAuthority(authority));
                        logger.debug("Added client authority: {}", authority);
                    }
                }
            }
            
            logger.debug("Final authorities for user {}: {}", jwt.getSubject(), authorities);
            return authorities;
        });
        
        return jwtConverter;
    }
}