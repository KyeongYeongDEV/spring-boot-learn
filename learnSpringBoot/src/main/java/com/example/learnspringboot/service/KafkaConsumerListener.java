package com.example.learnspringboot.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerListener {
    @KafkaListener(topics = "post-topic", groupId = "post-group")
    public void listen(ConsumerRecord<String, String> record) {
        System.out.println("Kafka 수신 메세지 : " + record.value());
    }
}