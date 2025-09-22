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
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //     http
    //         .authorizeHttpRequests(auth -> auth
    //                 .requestMatchers("/actuator/**").permitAll()
    //                 .requestMatchers("/api/admin/auth/**").permitAll()
    //                 .requestMatchers("/api/admin/**").hasRole("ADMIN")
    //                 .anyRequest().authenticated()
    //         )
    //         .oauth2ResourceServer(oauth2 -> oauth2
    //                 .jwt(jwt -> jwt
    //                         .jwtAuthenticationConverter(jwtAuthenticationConverter())
    //                         .decoder(jwtDecoder())
    //                 ));

    //     return http.build();
    // }

     @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // ✅ ADD THIS: Disable CSRF for API endpoints
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/admin/auth/**").permitAll() // ✅ This should permit login
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

    // @Bean
    // public JwtDecoder jwtDecoder() {
    //     // Use the JWK Set URI directly - this will work regardless of issuer mismatch
    //     logger.debug("Configuring JwtDecoder with custom JWK Set URI");
    //     NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
    //             .withJwkSetUri("http://keycloak:8080/realms/stockfellow/protocol/openid-connect/certs")
    //             .build();
        
    //     // Create a minimal validator that skips issuer validation for now
    //     OAuth2TokenValidator<Jwt> withoutIssuer = JwtValidators.createDefaultWithIssuer("");
    //     jwtDecoder.setJwtValidator(withoutIssuer);
        
    //     return jwtDecoder;
    // }
    // @Bean
    // public JwtDecoder jwtDecoder() {
    //     logger.debug("Configuring JwtDecoder with custom JWK Set URI");
    //     NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
    //             .withJwkSetUri("http://localhost:8080/realms/stockfellow/protocol/openid-connect/certs")
    //             .build();
        
    //     OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefault();  // Only timestamp validation
    //     jwtDecoder.setJwtValidator(validator);
        
    //     return jwtDecoder;
    // }

   @Bean
    public JwtDecoder jwtDecoder() {
        logger.debug("Configuring JwtDecoder with custom JWK Set URI");
        
        // Use localhost to match your test environment
        String jwkSetUri = "http://localhost:8080/realms/stockfellow/protocol/openid-connect/certs";
        logger.info("JWK Set URI: {}", jwkSetUri);
        
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();
        
        // Create a custom validator that accepts multiple issuers
        OAuth2TokenValidator<Jwt> validator = new OAuth2TokenValidator<Jwt>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt jwt) {
                // Accept both 'keycloak' and 'localhost' as valid issuers
                String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
                if (issuer.equals("http://keycloak:8080/realms/stockfellow") || 
                    issuer.equals("http://localhost:8080/realms/stockfellow")) {
                    logger.debug("Issuer validation passed for: {}", issuer);
                    return OAuth2TokenValidatorResult.success();
                }
                logger.warn("Issuer validation failed for: {}", issuer);
                
                // ✅ CORRECTED: Use OAuth2Error instead of Exception
                OAuth2Error error = new OAuth2Error("invalid_issuer", "Invalid token issuer: " + issuer, null);
                return OAuth2TokenValidatorResult.failure(error);
            }
        };
        
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        logger.debug("Configuring JwtAuthenticationConverter");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            logger.debug("Processing JWT claims for authorities");
            logger.debug("Full JWT claims: {}", jwt.getClaims());
            
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            
            // Extract realm roles from realm_access.roles
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                logger.debug("Found realm roles: {}", roles);
                
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    logger.debug("Added authority: ROLE_{}", role);
                }
            }
            
            // Also check resource_access for client-specific roles
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                logger.debug("Resource access found: {}", resourceAccess.keySet());
            }
            
            logger.debug("Final authorities: {}", authorities);
            return authorities;
        });
        
        return jwtConverter;
    }
}