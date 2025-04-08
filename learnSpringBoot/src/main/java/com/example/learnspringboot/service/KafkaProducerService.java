package com.example.learnspringboot.service;

import com.example.learnspringboot.dto.MessageDto;
import com.example.learnspringboot.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Post> kafkaTemplate;
    private static final String TOPIC = "post-topic";

    public void send(Post post) {
        kafkaTemplate.send("post-topic", post);
        System.out.println("✅ Kafka 메시지 전송: " + post.getTitle());
    }
}
