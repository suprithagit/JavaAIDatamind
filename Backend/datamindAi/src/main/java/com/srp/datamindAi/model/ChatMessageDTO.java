package com.srp.datamindAi.model;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for ChatMessage frontend communication.
 * Follows global standards for API data exchange.
 *
 * @author DataMind AI Backend Team
 * @version 2.0
 */
public class ChatMessageDTO {

    private String id;
    private String role;
    private String content;
    private ChartData chartData;
    private LocalDateTime timestamp;

    // Default constructor
    public ChatMessageDTO() {}

    // Constructor
    public ChatMessageDTO(String id, String role, String content, ChartData chartData, LocalDateTime timestamp) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.chartData = chartData;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public ChartData getChartData() { return chartData; }
    public void setChartData(ChartData chartData) { this.chartData = chartData; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("ChatMessageDTO{id='%s', role='%s', content='%s...', timestamp=%s}",
                           id, role, content.substring(0, Math.min(50, content.length())), timestamp);
    }
}