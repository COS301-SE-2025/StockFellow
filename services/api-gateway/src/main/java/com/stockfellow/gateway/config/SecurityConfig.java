package com.stockfellow.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/users/register").permitAll()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/login", "/register", "/logout").permitAll()
                .anyRequest().permitAll() 
            .and()
            .csrf().disable()
            .cors().and()
            .httpBasic().disable()
            .formLogin().disable();
    }
}