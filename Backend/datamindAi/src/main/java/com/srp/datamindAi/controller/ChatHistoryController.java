package com.srp.datamindAi.controller;

import com.srp.datamindAi.model.ChatMessageDTO;
import com.srp.datamindAi.service.ChatHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * REST controller for chat history operations.
 * Implements global standards for API design and security.
 *
 * @author DataMind AI Backend Team
 * @version 2.0
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "${app.frontend-url}")
public class ChatHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(ChatHistoryController.class);

    private final ChatHistoryService chatHistoryService;

    public ChatHistoryController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
        logger.info("ChatHistoryController initialized");
    }

    /**
     * Get chat history for a session.
     *
     * @param sessionId the session identifier
     * @return list of chat messages
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<?> getChatHistory(@PathVariable String sessionId) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Session ID cannot be empty"));
            }

            List<ChatMessageDTO> history = chatHistoryService.getChatHistoryDTO(sessionId);

            return ResponseEntity.ok(Map.of(
                "message", "Chat history retrieved successfully",
                "data", history,
                "count", history.size()
            ));

        } catch (Exception e) {
            logger.error("Error retrieving chat history for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve chat history"));
        }
    }

    /**
     * Get message count for a session.
     *
     * @param sessionId the session identifier
     * @return message count
     */
    @GetMapping("/count/{sessionId}")
    public ResponseEntity<?> getMessageCount(@PathVariable String sessionId) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Session ID cannot be empty"));
            }

            long count = chatHistoryService.getMessageCount(sessionId);

            return ResponseEntity.ok(Map.of(
                "message", "Message count retrieved successfully",
                "count", count
            ));

        } catch (Exception e) {
            logger.error("Error retrieving message count for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve message count"));
        }
    }

    /**
     * Save a user message (called by frontend before sending to query endpoint).
     *
     * @param request the save request
     * @param httpRequest HTTP servlet request for metadata
     * @return success response
     */
    @PostMapping("/save/user")
    public ResponseEntity<?> saveUserMessage(
            @RequestBody SaveMessageRequest request,
            HttpServletRequest httpRequest) {
        try {
            validateSaveRequest(request);

            String userAgent = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);

            chatHistoryService.saveUserMessage(
                request.getMessageId(),
                request.getSessionId(),
                request.getContent(),
                userAgent,
                ipAddress
            );

            return ResponseEntity.ok(Map.of(
                "message", "User message saved successfully"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error saving user message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to save user message"));
        }
    }

    /**
     * Save an assistant message (called by backend after processing query).
     *
     * @param request the save request
     * @return success response
     */
    @PostMapping("/save/assistant")
    public ResponseEntity<?> saveAssistantMessage(@RequestBody SaveAssistantMessageRequest request) {
        try {
            validateAssistantSaveRequest(request);

            chatHistoryService.saveAssistantMessage(
                request.getMessageId(),
                request.getSessionId(),
                request.getContent(),
                request.getChartData()
            );

            return ResponseEntity.ok(Map.of(
                "message", "Assistant message saved successfully"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error saving assistant message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to save assistant message"));
        }
    }

    /**
     * Request DTOs
     */
    public static class SaveMessageRequest {
        private String messageId;
        private String sessionId;
        private String content;

        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class SaveAssistantMessageRequest {
        private String messageId;
        private String sessionId;
        private String content;
        private com.srp.datamindAi.model.ChartData chartData;

        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public com.srp.datamindAi.model.ChartData getChartData() { return chartData; }
        public void setChartData(com.srp.datamindAi.model.ChartData chartData) { this.chartData = chartData; }
    }

    /**
     * Validation methods
     */
    private void validateSaveRequest(SaveMessageRequest request) {
        if (request.getMessageId() == null || request.getMessageId().trim().isEmpty()) {
            throw new IllegalArgumentException("Message ID cannot be empty");
        }
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be empty");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        if (request.getContent().length() > 5000) {
            throw new IllegalArgumentException("Content cannot exceed 5000 characters");
        }
    }

    private void validateAssistantSaveRequest(SaveAssistantMessageRequest request) {
        if (request.getMessageId() == null || request.getMessageId().trim().isEmpty()) {
            throw new IllegalArgumentException("Message ID cannot be empty");
        }
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be empty");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}