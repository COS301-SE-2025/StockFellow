package com.stockfellow.adminservice.service;

import com.stockfellow.adminservice.model.DailyMetrics;
import com.stockfellow.adminservice.repository.DailyMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@Service
public class DataSeedingService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeedingService.class);

    @Autowired
    private DailyMetricsRepository metricsRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only seed if table is empty
        if (metricsRepository.count() == 0) {
            logger.info("Seeding DailyMetrics table with sample data...");
            seedDailyMetrics();
            logger.info("Seeding completed.");
        } else {
            logger.info("DailyMetrics table already contains data, skipping seeding.");
        }
    }

    private void seedDailyMetrics() {
        Random random = new Random();
        LocalDate startDate = LocalDate.now().minusDays(90);
        
        for (int i = 0; i < 90; i++) {
            LocalDate date = startDate.plusDays(i);
            
            DailyMetrics metrics = new DailyMetrics();
            metrics.setDate(date);
            
            // Generate realistic sample data with some growth trends
            int baseUsers = 50 + (i / 3); // Growing user base
            int baseGroups = 5 + (i / 10); // Slower growing group count
            int baseTransactions = 20 + (i / 2); // Growing transaction count
            
            // Add some randomness
            metrics.setNewUsers((long) (baseUsers + random.nextInt(20) - 10));
            metrics.setActiveUsers((long) (baseUsers * 8 + random.nextInt(100)));
            metrics.setNewGroups((long) (baseGroups + random.nextInt(5)));
            metrics.setTotalTransactions((long) (baseTransactions + random.nextInt(30)));
            
            // Generate transaction volume (R1000-R50000 per day)
            double volume = 1000 + (random.nextDouble() * 49000);
            metrics.setTransactionVolume(BigDecimal.valueOf(volume));
            
            try {
                metricsRepository.save(metrics);
            } catch (Exception e) {
                logger.error("Failed to save metrics for date {}: {}", date, e.getMessage());
            }
        }
    }
}
