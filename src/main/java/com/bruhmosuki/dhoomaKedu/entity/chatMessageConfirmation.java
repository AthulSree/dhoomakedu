package com.bruhmosuki.dhoomaKedu.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message_confirmation")
public class chatMessageConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "recipient_ip", nullable = false, length = 50)
    private String recipientIp;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // "PENDING", "OK", "CANCEL"

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public chatMessageConfirmation() {
    }

    public chatMessageConfirmation(Long messageId, String recipientIp, String recipientName, String status, LocalDateTime respondedAt) {
        this.messageId = messageId;
        this.recipientIp = recipientIp;
        this.recipientName = recipientName;
        this.status = status;
        this.respondedAt = respondedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getRecipientIp() {
        return recipientIp;
    }

    public void setRecipientIp(String recipientIp) {
        this.recipientIp = recipientIp;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    @Override
    public String toString() {
        return "chatMessageConfirmation{" +
                "id=" + id +
                ", messageId=" + messageId +
                ", recipientIp='" + recipientIp + '\'' +
                ", recipientName='" + recipientName + '\'' +
                ", status='" + status + '\'' +
                ", respondedAt=" + respondedAt +
                '}';
    }
}
