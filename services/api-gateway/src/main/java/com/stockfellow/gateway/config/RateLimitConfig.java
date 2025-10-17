package com.stockfellow.gateway.config;

import com.stockfellow.gateway.filter.RateLimitFilter;
import com.stockfellow.gateway.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RateLimitConfig {
    
    @Autowired
    private List<Route> routes;
    
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(new RateLimitFilter(routes));
        registrationBean.addUrlPatterns("/api/*"); // Apply to all API routes
        registrationBean.setOrder(2); // Execute after auth filter
        registrationBean.setName("rateLimitFilter");
        
        return registrationBean;
    }
}