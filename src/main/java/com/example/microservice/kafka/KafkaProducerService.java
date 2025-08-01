package com.example.microservice.kafka;

import com.example.microservice.model.MessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, MessageRequest> kafkaTemplate;

    @Value("${kafka.topic.output}")
    private String outputTopic;

    public void sendMessage(String key, MessageRequest message) {
        CompletableFuture<org.springframework.kafka.support.SendResult<String, MessageRequest>> future = kafkaTemplate.send(outputTopic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent message to topic: {}, partition: {}, offset: {}, key: {}, ID: {}",
                        outputTopic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset(), key, message.getId());
            } else {
                logger.error("Failed to send message to topic: {}, key: {}, ID: {}. Error: {}", outputTopic, key, message.getId(), ex.getMessage());
            }
        });
    }
}