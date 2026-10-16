package com.bruhmosuki.dhoomaKedu.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy_acknowledgment")
public class policyAcknowledgment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 60)
    private String ipAddress;

    @Column(name = "acknowledged_date", nullable = false)
    private LocalDate acknowledgedDate;

    @Column(name = "acknowledged_at", nullable = false)
    private LocalDateTime acknowledgedAt;

    public policyAcknowledgment() {
    }

    public policyAcknowledgment(String ipAddress, LocalDate acknowledgedDate, LocalDateTime acknowledgedAt) {
        this.ipAddress = ipAddress;
        this.acknowledgedDate = acknowledgedDate;
        this.acknowledgedAt = acknowledgedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDate getAcknowledgedDate() {
        return acknowledgedDate;
    }

    public void setAcknowledgedDate(LocalDate acknowledgedDate) {
        this.acknowledgedDate = acknowledgedDate;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }
}
