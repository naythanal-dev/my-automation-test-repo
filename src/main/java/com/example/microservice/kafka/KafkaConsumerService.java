package com.example.microservice.kafka;

import com.example.microservice.model.MessageRequest;
import com.example.microservice.service.MessageProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final MessageProcessingService messageProcessingService;

    @Value("${kafka.topic.input}")
    private String inputTopic;

    public KafkaConsumerService(MessageProcessingService messageProcessingService) {
        this.messageProcessingService = messageProcessingService;
    }

    @KafkaListener(topics = "${kafka.topic.input}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, MessageRequest> record,
                       Acknowledgment acknowledgment,
                       @Header(KafkaHeaders.RECEIVED_KEY) String messageKey,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partitionId) {
        try {
            MessageRequest messageRequest = record.value();
            logger.info("Received message from topic {}: {} with key {}, partition {}", inputTopic, messageRequest, messageKey, partitionId);

            messageProcessingService.processMessage(messageRequest);

            acknowledgment.acknowledge();
            logger.info("Message acknowledged from topic {}: {}", inputTopic, messageRequest.getId());

        } catch (Exception e) {
            logger.error("Error processing message from topic {} with key {}, partition {}: {}", inputTopic, messageKey, partitionId, record.value(), e);
            // Decide whether to acknowledge or not based on the error
            // If you don't acknowledge, the message will be redelivered
             acknowledgment.acknowledge(); // Acknowledge to avoid re-delivery for unrecoverable errors
        }
    }
}