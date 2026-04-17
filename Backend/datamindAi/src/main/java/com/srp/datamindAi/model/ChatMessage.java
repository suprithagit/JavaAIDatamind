package com.srp.datamindAi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ChatMessage entity for storing chat history in Supabase PostgreSQL database.
 * Implements global standards for data persistence and accessibility.
 *
 * @author DataMind AI Backend Team
 * @version 2.0
 */
@Entity
@Table(name = "chat_messages", schema = "public")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true, length = 36)
    private String messageId;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MessageRole role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "chart_type")
    private String chartType;

    @Column(name = "chart_title")
    private String chartTitle;

    @Column(name = "chart_insight", columnDefinition = "TEXT")
    private String chartInsight;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    // Default constructor
    public ChatMessage() {}

    // Constructor for user messages
    public ChatMessage(String messageId, String sessionId, MessageRole role, String content, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Constructor for assistant messages with chart data
    public ChatMessage(String messageId, String sessionId, MessageRole role, String content,
                      String chartType, String chartTitle, String chartInsight, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.chartType = chartType;
        this.chartTitle = chartTitle;
        this.chartInsight = chartInsight;
        this.timestamp = timestamp;
    }

    public enum MessageRole {
        user, assistant
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public MessageRole getRole() { return role; }
    public void setRole(MessageRole role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }

    public String getChartTitle() { return chartTitle; }
    public void setChartTitle(String chartTitle) { this.chartTitle = chartTitle; }

    public String getChartInsight() { return chartInsight; }
    public void setChartInsight(String chartInsight) { this.chartInsight = chartInsight; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    @Override
    public String toString() {
        return String.format("ChatMessage{id=%d, messageId='%s', role=%s, content='%s...', timestamp=%s}",
                           id, messageId, role, content.substring(0, Math.min(50, content.length())), timestamp);
    }
}