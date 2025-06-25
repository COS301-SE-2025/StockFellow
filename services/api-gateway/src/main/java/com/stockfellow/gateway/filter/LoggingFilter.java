package com.stockfellow.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Continue with the request
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(httpRequest, httpResponse, duration);
        }
    }
    
    private void logRequest(HttpServletRequest request, HttpServletResponse response, long duration) {
        String logMessage = String.format("%s %s %d %dms",
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            duration
        );
        
        if (response.getStatus() >= 400) {
            logger.warn(logMessage);
        } else {
            logger.info(logMessage);
        }
    }
}