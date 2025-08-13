package com.example.microservice.service;

import com.example.microservice.model.MessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MessageProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessingService.class);

    private final KafkaProducerService kafkaProducerService;

    public MessageProcessingService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Async("messageProcessingExecutor")
    public void processMessage(MessageRequest messageRequest) {
        try {
            logger.info("Processing message: {}", messageRequest);

            // Simulate business logic
            String partnerName = messageRequest.getHeaders() != null ? messageRequest.getHeaders().get("partnerName") : null;
            String serviceFlag = messageRequest.getHeaders() != null ? messageRequest.getHeaders().get("serviceFlag") : null;

            if ("NAVI".equals(partnerName) && "N".equals(serviceFlag)) {
                logger.warn("Skipping Navi loan with serviceFlag 'N': {}", messageRequest.getId());
                return;
            }

            String processedPayload = "Processed: " + messageRequest.getPayload();
            messageRequest.setPayload(processedPayload);

            // Send the processed message to the output topic
            kafkaProducerService.sendMessage(messageRequest.getId(), messageRequest);

            logger.info("Message processed and sent to output topic: {}", messageRequest.getId());
        } catch (Exception e) {
            logger.error("Error processing message: {}", messageRequest, e);
        }
    }
}