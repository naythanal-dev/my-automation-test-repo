package com.example.microservice.kafka;

import com.example.microservice.model.MessageRequest;
import com.example.microservice.service.MessageProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import org.springframework.kafka.support.Acknowledgment;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private MessageProcessingService messageProcessingService;

    @Value("${kafka.topic.input}")
    private String inputTopic;

    @KafkaListener(topics = "${kafka.topic.input}", groupId = "${kafka.consumer.group-id}")
    public void listen(org.apache.kafka.clients.consumer.ConsumerRecord<String, MessageRequest> record,
                       Acknowledgment acknowledgment,
                       @Header(KafkaHeaders.RECEIVED_KEY) String messageKey,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partitionId) {
        try {
            MessageRequest message = record.value();
            logger.info("Received message with ID: {}, key: {}, partition: {}", message.getId(), messageKey, partitionId);

            CompletableFuture<Void> future = messageProcessingService.processMessage(message, messageKey, partitionId);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    acknowledgment.acknowledge();
                    logger.info("Message with ID: {} acknowledged", message.getId());
                } else {
                    logger.error("Error processing message with ID: {}.  Replaying. Error: {}", message.getId(), ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error processing message key: {}, partition: {}. Error: {}", messageKey, partitionId, e.getMessage(), e);
            // Decide whether to acknowledge or not based on the error.
            // If you want to retry the message, do not acknowledge.
             acknowledgment.acknowledge(); // Acknowledge to avoid re-processing the message in case of persistent errors.
        }
    }
}