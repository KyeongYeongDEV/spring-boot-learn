package com.example.learnspringboot.service;

import com.example.learnspringboot.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, MessageDto> kafkaTemplate;
    private static final String TOPIC = "message-topic";
    //private static final String TOPIC = "post-topic"; // 사용할 Kafka 토픽

    public void send(MessageDto messaage) {
        kafkaTemplate.send(TOPIC, message.getId(), messaage);



    // 메시지 전송 메소드
//    public void sendMessage(String message) {
//        System.out.println("Kafka 전송 메시지 ▶️ " + message);
//        kafkaTemplate.send(TOPIC, message);
    }
}
