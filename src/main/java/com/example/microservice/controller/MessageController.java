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
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageProcessingService messageProcessingService;

    @PostMapping("/process")
    public ResponseEntity<String> processMessage(@Valid @RequestBody MessageRequest messageRequest) {
        try {
            logger.info("Received REST request to process message: {}", messageRequest.getId());
            messageProcessingService.processMessage(messageRequest);
            return new ResponseEntity<>("Message processing initiated", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            logger.error("Error processing REST request: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error processing message", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
