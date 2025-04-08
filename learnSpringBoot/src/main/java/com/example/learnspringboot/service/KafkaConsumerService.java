package com.example.learnspringboot.service;

import com.example.learnspringboot.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final RedisService redisService;

    @KafkaListener(topics = "message-topic", groupId = "message-group")
    public void listen(MessageDto message) {
        System.out.println("Kafka Message Received: " + message);

        // Redis에 저장
        String result = "처리됨 : " + message.getContent();
        redisService.setData(message.getId(), result);
    }


//    @KafkaListener(topics = "post-topic", groupId = "post-group")
//    public void consume(String message) {
//        System.out.println("Kafka에서 메세지 수신됨" + message);
//    }
}
