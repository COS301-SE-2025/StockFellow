package com.stockfellow.gateway.config;

import com.stockfellow.gateway.filter.LoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {
    
    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter() {
        FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(new LoggingFilter());
        registrationBean.addUrlPatterns("/*"); // Apply to all routes (like morgan)
        registrationBean.setOrder(0); // Execute first, before rate limiting
        registrationBean.setName("loggingFilter");
        
        return registrationBean;
    }
}