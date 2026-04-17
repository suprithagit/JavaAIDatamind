package com.srp.datamindAi.controller;

import com.srp.datamindAi.model.AppConfigResponse;
import com.srp.datamindAi.model.DashboardOverview;
import com.srp.datamindAi.model.QueryRequest;
import com.srp.datamindAi.model.QueryResponse;
import com.srp.datamindAi.service.ChatHistoryService;
import com.srp.datamindAi.service.QueryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * QueryController - REST API endpoints for queries and file uploads
 */
@RestController
@RequestMapping("/api")
public class QueryController {
    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    private static final String ALLOWED_MIME_TYPES = "text/csv,application/vnd.ms-excel,application/csv,text/plain,application/octet-stream";

    private final QueryService queryService;
    private final ChatHistoryService chatHistoryService;

    public QueryController(QueryService queryService, ChatHistoryService chatHistoryService) {
        this.queryService = queryService;
        this.chatHistoryService = chatHistoryService;
    }

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody QueryRequest request) {
        try {
            if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                logger.warn("Invalid query request: empty query");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Query cannot be empty", 400));
            }

            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = "default-session-" + System.currentTimeMillis();
                logger.info("No session ID provided, using generated: {}", sessionId);
            }

            String messageId = request.getMessageId();
            if (messageId == null || messageId.trim().isEmpty()) {
                messageId = java.util.UUID.randomUUID().toString();
                logger.debug("No message ID provided, generated: {}", messageId);
            }

            logger.info("Processing query: {} (session: {}, message: {})",
                       request.getQuery().substring(0, Math.min(50, request.getQuery().length())),
                       sessionId, messageId);

            // Save user message to database
            try {
                chatHistoryService.saveUserMessage(messageId, sessionId, request.getQuery().trim(), null, null);
                logger.debug("User message saved to database: {}", messageId);
            } catch (Exception e) {
                logger.warn("Failed to save user message to database: {}", e.getMessage());
                // Continue processing even if database save fails
            }

            QueryResponse response = queryService.processQuery(request);

            // Save assistant response to database
            try {
                String assistantMessageId = java.util.UUID.randomUUID().toString();
                chatHistoryService.saveAssistantMessage(assistantMessageId, sessionId, response.getSummary(), response.getChartData());
                logger.debug("Assistant message saved to database: {}", assistantMessageId);
            } catch (Exception e) {
                logger.warn("Failed to save assistant message to database: {}", e.getMessage());
                // Continue processing even if database save fails
            }

            return ResponseEntity.ok(response);

        } catch (Exception exception) {
            logger.error("Error processing query", exception);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error processing query: " + exception.getMessage(), 500));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        try {
            logger.info("Retrieving dashboard overview");
            DashboardOverview overview = queryService.getDashboardOverview();
            return ResponseEntity.ok(overview);
            
        } catch (Exception exception) {
            logger.error("Error retrieving dashboard overview", exception);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error retrieving dashboard", 500));
        }
    }

    @GetMapping("/config")
    public ResponseEntity<?> config() {
        try {
            logger.info("Retrieving application configuration");
            AppConfigResponse configResponse = queryService.getAppConfig();
            return ResponseEntity.ok(configResponse);
            
        } catch (Exception exception) {
            logger.error("Error retrieving configuration", exception);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error retrieving configuration", 500));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                logger.warn("Upload attempt with empty or null file");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("File is required and cannot be empty", 400));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                logger.warn("Upload attempt with oversized file: {} bytes", file.getSize());
                return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(new ErrorResponse("File size exceeds maximum allowed (50 MB)", 413));
            }

            String contentType = file.getContentType();
            if (contentType == null || !isAllowedContentType(contentType)) {
                logger.warn("Upload attempt with invalid content type: {}", contentType);
                return ResponseEntity
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(new ErrorResponse("Only CSV files are allowed. Received: " + contentType, 415));
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
                logger.warn("Upload attempt with invalid extension: {}", filename);
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("File must have .csv extension", 400));
            }

            logger.info("Processing CSV upload: {}", filename);
            queryService.loadCsv(file.getInputStream());
            
            String successMessage = String.format(
                "CSV uploaded successfully: '%s' parsed and ready for analysis.",
                filename
            );
            logger.info(successMessage);
            
            return ResponseEntity.ok(new SuccessResponse(successMessage));
            
        } catch (IOException ioException) {
            logger.error("IO error while processing CSV upload", ioException);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error reading file: " + ioException.getMessage(), 500));
                
        } catch (Exception exception) {
            logger.error("Unexpected error while processing CSV upload", exception);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error processing CSV: " + exception.getMessage(), 500));
        }
    }

    private boolean isAllowedContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        String baseType = contentType.split(";")[0].trim();
        
        return baseType.equals("text/csv") || 
               baseType.equals("application/vnd.ms-excel") ||
               baseType.equals("application/csv") ||
               baseType.equals("text/plain") ||
               baseType.equals("application/octet-stream");
    }

    public static class ErrorResponse {
        public final String error;
        public final int statusCode;

        public ErrorResponse(String error, int statusCode) {
            this.error = error;
            this.statusCode = statusCode;
        }

        public String getError() {
            return error;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public static class SuccessResponse {
        public final String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
