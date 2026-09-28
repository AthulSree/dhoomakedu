package com.bruhmosuki.dhoomaKedu.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "chat_message")
public class chatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_ip", nullable = false, length = 50)
    private String senderIp;

    @Column(name = "sender_name", nullable = false, length = 100)
    private String senderName;

    @Column(name = "recipient_ip", length = 50)
    private String recipientIp;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "message_content", nullable = false, length = 1000)
    private String messageContent;

    @Column(name = "msg_type", nullable = false, length = 20)
    private String msgType;

    @Column(name = "group_name", length = 50)
    private String groupName;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Transient
    private List<chatMessageConfirmation> confirmations = new ArrayList<>();

    public chatMessage() {
    }

    public chatMessage(String senderIp, String senderName, String recipientIp, String recipientName,
                       String messageContent, String msgType, String groupName, LocalDateTime timestamp) {
        this.senderIp = senderIp;
        this.senderName = senderName;
        this.recipientIp = recipientIp;
        this.recipientName = recipientName;
        this.messageContent = messageContent;
        this.msgType = msgType;
        this.groupName = groupName;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<chatMessageConfirmation> getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(List<chatMessageConfirmation> confirmations) {
        this.confirmations = confirmations;
    }

    @Override
    public String toString() {
        return "chatMessage{" +
                "id=" + id +
                ", senderIp='" + senderIp + '\'' +
                ", senderName='" + senderName + '\'' +
                ", recipientIp='" + recipientIp + '\'' +
                ", recipientName='" + recipientName + '\'' +
                ", messageContent='" + messageContent + '\'' +
                ", msgType='" + msgType + '\'' +
                ", groupName='" + groupName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
