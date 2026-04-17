package com.srp.datamindAi.repository;

import com.srp.datamindAi.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for ChatMessage entity operations.
 * Provides database access methods following global standards.
 *
 * @author DataMind AI Backend Team
 * @version 2.0
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Find all messages for a specific session ordered by timestamp.
     *
     * @param sessionId the session identifier
     * @return list of chat messages in chronological order
     */
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);

    /**
     * Find messages for a session within a time range.
     *
     * @param sessionId the session identifier
     * @param startTime start of time range
     * @param endTime end of time range
     * @return list of chat messages within the time range
     */
    List<ChatMessage> findBySessionIdAndTimestampBetweenOrderByTimestampAsc(
        String sessionId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Count messages for a session.
     *
     * @param sessionId the session identifier
     * @return number of messages in the session
     */
    long countBySessionId(String sessionId);

    /**
     * Find recent messages across all sessions (for analytics).
     *
     * @param since timestamp to search from
     * @return list of recent messages
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<ChatMessage> findRecentMessages(@Param("since") LocalDateTime since);

    /**
     * Delete old messages (cleanup utility).
     *
     * @param before timestamp before which to delete
     * @return number of deleted messages
     */
    long deleteByTimestampBefore(LocalDateTime before);
}