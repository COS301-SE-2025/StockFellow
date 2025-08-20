package com.stockfellow.userservice.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1000)) {
                return Health.up().withDetail("database", "PostgreSQL").build();
            }
            return Health.down().withDetail("database", "Connection invalid").build();
        } catch (Exception e) {
            return Health.down(e)
                   .withDetail("database", "PostgreSQL")
                   .withDetail("error", e.getMessage())
                   .build();
        }
    }
}