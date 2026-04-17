package com.srp.datamindAi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * QueryRequest represents a natural language query request from the frontend.
 * 
 * Provides:
 * - Type-safe query encapsulation
 * - JSON serialization support
 * - Session and message tracking for chat history
 * - Clear documentation of API contract
 * 
 * Example Request Body:
 * {
 *   "query": "Show me sales by region",
 *   "sessionId": "session-123",
 *   "messageId": "msg-456"
 * }
 * 
 * @author DataMind AI Backend Team
 * @version 2.0
 */
public class QueryRequest {

    @JsonProperty("query")
    private String query;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("messageId")
    private String messageId;

    public QueryRequest() {
    }

    /**
     * Constructor for creating a query request.
     * 
     * @param query the natural language query string (required, non-empty)
     */
    public QueryRequest(String query) {
        this.query = query;
    }

    /**
     * Constructor with session and message tracking.
     * 
     * @param query the natural language query string
     * @param sessionId the chat session identifier
     * @param messageId the unique message identifier
     */
    public QueryRequest(String query, String sessionId, String messageId) {
        this.query = query;
        this.sessionId = sessionId;
        this.messageId = messageId;
    }

    /**
     * Gets the query string.
     * 
     * @return the query text
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query string.
     * 
     * @param query the query text (should be non-empty)
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Gets the session identifier for chat history tracking.
     * 
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session identifier.
     * 
     * @param sessionId the session ID for grouping chat messages
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the unique message identifier.
     * 
     * @return the message ID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the message identifier.
     * 
     * @param messageId unique identifier for this message
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "QueryRequest{" +
                "query='" + (query != null ? query.substring(0, Math.min(50, query.length())) + "..." : "null") + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", messageId='" + messageId + '\'' +
                '}';
    }
}
