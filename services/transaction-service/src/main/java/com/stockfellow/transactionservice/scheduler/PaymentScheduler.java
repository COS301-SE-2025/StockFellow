package com.stockfellow.transactionservice.scheduler;

import com.stockfellow.transactionservice.model.GroupCycle;
import com.stockfellow.transactionservice.repository.GroupCycleRepository;
import com.stockfellow.transactionservice.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PaymentScheduler {
    private static final Logger logger = LoggerFactory.getLogger(PaymentScheduler.class);

    @Autowired
    private GroupCycleRepository groupCycleRepository;
    @Autowired
    private TransactionService transactionService;

    @Scheduled(cron = "${scheduler.debit-order-cron}")
    public void processDebitOrders() {
        List<GroupCycle> groupCycles = groupCycleRepository.findByNextRunAndStatus(LocalDate.now(), "ACTIVE");
        for (GroupCycle groupCycle : groupCycles) {
            try {
                if (groupCycle.getType().equals("DEBIT_ORDER")) {
                    transactionService.processDebitOrder(groupCycle.getUserId(), groupCycle.getGroupId(),
                            groupCycle.getAmount());
                    updateNextRun(groupCycle);
                }
            } catch (Exception e) {
                logger.error("Debit order failed for groupCycle {}: {}", groupCycle.getScheduleId(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "${scheduler.payout-cron}")
    public void processPayouts() {
        List<GroupCycle> groupCycles = groupCycleRepository.findByNextRunAndStatus(LocalDate.now(), "ACTIVE");
        for (GroupCycle groupCycle : groupCycles) {
            try {
                if (groupCycle.getType().equals("PAYOUT")) {
                    transactionService.processPayout(groupCycle.getUserId(), groupCycle.getGroupId(),
                            groupCycle.getAmount());
                    updateNextRun(groupCycle);
                }
            } catch (Exception e) {
                logger.error("Payout failed for groupCycle {}: {}", groupCycle.getScheduleId(), e.getMessage());
            }
        }
    }

    private void updateNextRun(GroupCycle groupCycle) {
        LocalDate nextRun = groupCycle.getNextRun();
        switch (groupCycle.getFrequency()) {
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
        groupCycle.setNextRun(nextRun);
        groupCycleRepository.save(groupCycle);
    }
}
