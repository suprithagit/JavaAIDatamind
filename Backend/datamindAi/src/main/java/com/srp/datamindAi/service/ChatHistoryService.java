package com.srp.datamindAi.service;

import com.srp.datamindAi.model.ChatMessage;
import com.srp.datamindAi.model.ChartData;
import com.srp.datamindAi.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatHistoryService manages chat message persistence and retrieval.
 * Implements global standards for data operations and security.
 *
 * @author DataMind AI Backend Team
 * @version 2.0
 */
@Service
public class ChatHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(ChatHistoryService.class);

    private final ChatMessageRepository chatMessageRepository;

    public ChatHistoryService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
        logger.info("ChatHistoryService initialized");
    }

    /**
     * Save a user message to the database.
     *
     * @param messageId unique message identifier
     * @param sessionId session identifier
     * @param content message content
     * @param userAgent optional user agent string
     * @param ipAddress optional IP address
     * @return saved ChatMessage entity
     */
    @Transactional
    public ChatMessage saveUserMessage(String messageId, String sessionId, String content,
                                      String userAgent, String ipAddress) {
        ChatMessage message = new ChatMessage(
            messageId,
            sessionId,
            ChatMessage.MessageRole.user,
            content,
            LocalDateTime.now()
        );

        message.setUserAgent(userAgent);
        message.setIpAddress(ipAddress);

        ChatMessage saved = chatMessageRepository.save(message);
        logger.debug("Saved user message: {}", messageId);

        return saved;
    }

    /**
     * Save an assistant message to the database.
     *
     * @param messageId unique message identifier
     * @param sessionId session identifier
     * @param content message content
     * @param chartData optional chart data
     * @return saved ChatMessage entity
     */
    @Transactional
    public ChatMessage saveAssistantMessage(String messageId, String sessionId, String content,
                                          ChartData chartData) {
        ChatMessage message = new ChatMessage(
            messageId,
            sessionId,
            ChatMessage.MessageRole.assistant,
            content,
            chartData != null ? chartData.getType() : null,
            chartData != null ? chartData.getTitle() : null,
            chartData != null ? chartData.getInsight() : null,
            LocalDateTime.now()
        );

        ChatMessage saved = chatMessageRepository.save(message);
        logger.debug("Saved assistant message: {}", messageId);

        return saved;
    }

    /**
     * Retrieve chat history for a session.
     *
     * @param sessionId session identifier
     * @return list of chat messages in chronological order
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatHistory(String sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        logger.debug("Retrieved {} messages for session: {}", messages.size(), sessionId);
        return messages;
    }

    /**
     * Get chat history formatted for frontend consumption.
     *
     * @param sessionId session identifier
     * @return list of frontend-compatible chat messages
     */
    @Transactional(readOnly = true)
    public List<com.srp.datamindAi.model.ChatMessageDTO> getChatHistoryDTO(String sessionId) {
        List<ChatMessage> messages = getChatHistory(sessionId);

        return messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Count messages in a session.
     *
     * @param sessionId session identifier
     * @return number of messages
     */
    @Transactional(readOnly = true)
    public long getMessageCount(String sessionId) {
        return chatMessageRepository.countBySessionId(sessionId);
    }

    /**
     * Clean up old messages (utility method).
     *
     * @param daysOld messages older than this many days will be deleted
     * @return number of deleted messages
     */
    @Transactional
    public long cleanupOldMessages(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        long deleted = chatMessageRepository.deleteByTimestampBefore(cutoff);
        logger.info("Cleaned up {} messages older than {} days", deleted, daysOld);
        return deleted;
    }

    /**
     * Convert entity to DTO for frontend.
     */
    private com.srp.datamindAi.model.ChatMessageDTO convertToDTO(ChatMessage entity) {
        com.srp.datamindAi.model.ChatMessageDTO dto = new com.srp.datamindAi.model.ChatMessageDTO();
        dto.setId(entity.getMessageId());
        dto.setRole(entity.getRole().name());
        dto.setContent(entity.getContent());
        dto.setTimestamp(entity.getTimestamp());

        // Convert chart data if present
        if (entity.getChartType() != null) {
            ChartData chartData = new ChartData();
            chartData.setType(entity.getChartType());
            chartData.setTitle(entity.getChartTitle());
            chartData.setInsight(entity.getChartInsight());
            // Note: labels and datasets are not stored, would need enhancement
            dto.setChartData(chartData);
        }

        return dto;
    }
}