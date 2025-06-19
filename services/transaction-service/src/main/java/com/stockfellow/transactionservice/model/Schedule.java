package com.stockfellow.transactionservice.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @Column(name = "schedule_id")
    private String scheduleId;
    @Column(name = "group_id")
    private String groupId;
    @Column(name = "user_id")
    private String userId;
    private String type;
    private Double amount;
    private String frequency;
    @Column(name = "next_run")
    private LocalDate nextRun;
    private String status;
}