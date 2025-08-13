package com.example.microservice.kafka;

import com.example.microservice.model.MessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    @Value("${kafka.topic.output}")
    private String outputTopic;

    public KafkaProducerService(KafkaTemplate<String, MessageRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String messageId, MessageRequest messageRequest) {
        CompletableFuture<SendResult<String, MessageRequest>> future = kafkaTemplate.send(outputTopic, messageId, messageRequest);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent message=[{}] with offset=[{}] to topic {}", messageRequest, result.getRecordMetadata().offset(), outputTopic);
            } else {
                logger.error("Unable to send message=[{}] due to : {}", messageRequest, ex.getMessage());
            }
        });
    }
}