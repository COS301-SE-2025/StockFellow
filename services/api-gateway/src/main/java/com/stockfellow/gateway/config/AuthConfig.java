package com.stockfellow.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockfellow.gateway.filter.AuthFilter;
import com.stockfellow.gateway.service.TokenValidationService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {

    private final TokenValidationService tokenValidationService;
    private final ObjectMapper objectMapper;

    public AuthConfig(TokenValidationService tokenValidationService, ObjectMapper objectMapper) {
        this.tokenValidationService = tokenValidationService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilter() {
        FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new AuthFilter(tokenValidationService, objectMapper));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        registrationBean.setName("authFilter");

        return registrationBean;
    }
}
