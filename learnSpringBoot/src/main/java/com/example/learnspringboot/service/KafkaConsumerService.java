package com.example.learnspringboot.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @KafkaListener(topics = "post-topic", groupId = "post-group")
    public void consume(String message) {
        System.out.println("Kafka에서 메세지 수신됨" + message);
    }
}
