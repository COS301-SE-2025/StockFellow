package com.stockfellow.transactionservice.scheduler;

import com.stockfellow.transactionservice.model.Schedule;
import com.stockfellow.transactionservice.repository.ScheduleRepository;
import com.stockfellow.transactionservice.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TransactionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TransactionScheduler.class);

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private TransactionService transactionService;

    @Scheduled(cron = "${scheduler.debit-order-cron}")
    public void processDebitOrders() {
        List<Schedule> schedules = scheduleRepository.findByNextRunAndStatus(LocalDate.now(), "ACTIVE");
        for (Schedule schedule : schedules) {
            try {
                if (schedule.getType().equals("DEBIT_ORDER")) {
                    transactionService.processDebitOrder(schedule.getUserId(), schedule.getGroupId(), schedule.getAmount());
                    updateNextRun(schedule);
                }
            } catch (Exception e) {
                logger.error("Debit order failed for schedule {}: {}", schedule.getScheduleId(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "${scheduler.payout-cron}")
    public void processPayouts() {
        List<Schedule> schedules = scheduleRepository.findByNextRunAndStatus(LocalDate.now(), "ACTIVE");
        for (Schedule schedule : schedules) {
            try {
                if (schedule.getType().equals("PAYOUT")) {
                    transactionService.processPayout(schedule.getUserId(), schedule.getGroupId(), schedule.getAmount());
                    updateNextRun(schedule);
                }
            } catch (Exception e) {
                logger.error("Payout failed for schedule {}: {}", schedule.getScheduleId(), e.getMessage());
            }
        }
    }

    private void updateNextRun(Schedule schedule) {
        LocalDate nextRun = schedule.getNextRun();
        switch (schedule.getFrequency()) {
            case "MONTHLY":
                nextRun = nextRun.plusMonths(1);
                break;
            case "BI_WEEKLY":
                nextRun = nextRun.plusWeeks(2);
                break;
            case "WEEKLY":
                nextRun = nextRun.plusWeeks(1);
                break;
        }
        schedule.setNextRun(nextRun);
        scheduleRepository.save(schedule);
    }
}
