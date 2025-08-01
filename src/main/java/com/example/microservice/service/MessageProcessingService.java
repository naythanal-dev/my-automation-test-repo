package com.example.microservice.service;

import com.example.microservice.model.MessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class MessageProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessingService.class);

    @Autowired
    private KafkaTemplate<String, MessageRequest> kafkaTemplate;

    @Value("${kafka.topic.output}")
    private String outputTopic;

    @Async("messageProcessorExecutor")
    public CompletableFuture<Void> processMessage(MessageRequest message, String messageKey, Integer partitionId) {
        try {
            logger.info("Processing message with ID: {}, key: {}, partition: {}", message.getId(), messageKey, partitionId);

            // Simulate some business logic
            String partnerName = message.getHeaders().getOrDefault("partnerName", "").toString();
            String serviceFlag = message.getHeaders().getOrDefault("serviceFlag", "Y").toString();

            // Filter Navi loans based on serviceFlag
            if (partnerName.equalsIgnoreCase("NAVI") && serviceFlag.equalsIgnoreCase("N")) {
                logger.warn("Skipping processing for Navi loan with serviceFlag=N, ID: {}", message.getId());
                return CompletableFuture.completedFuture(null); // Skip processing
            }

            message.setPayload("Processed: " + message.getPayload());

            // Send the processed message to the output topic
            String newKey = UUID.randomUUID().toString();
            CompletableFuture<org.springframework.kafka.support.SendResult<String, MessageRequest>> future = kafkaTemplate.send(outputTopic, newKey, message);

            return future.thenAccept(result -> {
                logger.info("Sent message to topic: {}, partition: {}, offset: {}, key: {}, ID: {}",
                        outputTopic,result.getRecordMetadata().partition(), result.getRecordMetadata().offset(), newKey, message.getId());
            }).exceptionally(ex -> {
                logger.error("Failed to send message to topic: {}, key: {}, ID: {}. Error: {}", outputTopic, newKey, message.getId(), ex.getMessage());
                return null;
            }).thenApply(v -> null); // Convert CompletableFuture<Void>.

        } catch (Exception e) {
            logger.error("Error processing message with ID: {}, key: {}, partition: {}. Error: {}", message.getId(), messageKey, partitionId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}