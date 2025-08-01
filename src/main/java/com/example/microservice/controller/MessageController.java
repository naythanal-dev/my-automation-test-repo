package com.example.microservice.controller;

import com.example.microservice.model.MessageRequest;
import com.example.microservice.service.MessageProcessingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageProcessingService messageProcessingService;

    @PostMapping("/process")
    public ResponseEntity<String> processMessage(@Valid @RequestBody MessageRequest message) {
        try {
            String messageKey = UUID.randomUUID().toString();
            Integer partitionId = 0; // Assuming partition 0 for simplicity

            logger.info("Received REST request to process message with ID: {}", message.getId());

            CompletableFuture<Void> future = messageProcessingService.processMessage(message, messageKey, partitionId);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Message processing initiated successfully for ID: {}", message.getId());
                } else {
                    logger.error("Error processing message with ID: {}. Error: {}", message.getId(), ex.getMessage());
                }
            });

            return ResponseEntity.ok("Message processing initiated successfully");

        } catch (Exception e) {
            logger.error("Error processing REST request. Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
        }
    }
}