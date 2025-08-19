package com.stockfellow.demoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DemoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoServiceApplication.class, args);
    }

    /**
     * RestTemplate bean for making HTTP calls to other microservices
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
