package com.example.learnspringboot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "post-log-topic";

    public void sendMessage(String message) {
        System.out.println("Kafka에 메시지 전송 : " + message);
        kafkaTemplate.send(TOPIC, message);
    }
}
