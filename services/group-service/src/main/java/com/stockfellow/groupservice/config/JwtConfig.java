package com.stockfellow.groupservice.config;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class JwtConfig {

    private static final String JWKS_URL = "http://localhost:8080/realms/stockfellow/protocol/openid-connect/certs";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/groups").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    public class JwtFilter extends OncePerRequestFilter {
        private final JwkProvider provider;

        public JwtFilter() {
            this.provider = new JwkProviderBuilder(JWKS_URL)
                    .cached(10, java.time.Duration.ofHours(1))
                    .build();
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            try {
                String token = authHeader.substring(7);
                DecodedJWT decodedJWT = JWT.decode(token);
                String kid = decodedJWT.getKeyId();

                Jwk jwk = provider.get(kid);
                RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();

                Algorithm algorithm = Algorithm.RSA256(publicKey, null);
                JWTVerifier verifier = JWT.require(algorithm).build();

                DecodedJWT verifiedJWT = verifier.verify(token);

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        verifiedJWT.getSubject(), null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
                filterChain.doFilter(request, response);
            } catch (JWTVerificationException | JwkException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token: " + e.getMessage());
            }
        }
    }
}
