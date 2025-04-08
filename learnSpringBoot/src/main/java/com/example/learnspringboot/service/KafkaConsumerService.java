package com.example.learnspringboot.service;

import com.example.learnspringboot.dto.MessageDto;
import com.example.learnspringboot.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "post-topic", groupId = "post-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(Post post) {
        System.out.println("Kafka 메시지 수신: " + post.getTitle());

        // Redis에 TTL과 함께 저장
        String key = "post_" + post.getId();
        redisTemplate.opsForValue().set(key, post, 60, TimeUnit.SECONDS);
        System.out.println("Redis 캐싱 완료: " + key);
    }
}
