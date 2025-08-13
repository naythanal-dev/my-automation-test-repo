package com.example.microservice.controller;

import com.example.microservice.model.MessageRequest;
import com.example.microservice.service.MessageProcessingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    private final MessageProcessingService messageProcessingService;

    public MessageController(MessageProcessingService messageProcessingService) {
        this.messageProcessingService = messageProcessingService;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processMessage(@Valid @RequestBody MessageRequest messageRequest, @RequestHeader Map<String, String> headers) {
        try {
            messageRequest.setHeaders(headers);
            logger.info("Received request to process message: {}", messageRequest);
            messageProcessingService.processMessage(messageRequest);
            return ResponseEntity.ok("Message processing initiated");
        } catch (Exception e) {
            logger.error("Error processing message via REST: {}", messageRequest, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing message");
        }
    }
}