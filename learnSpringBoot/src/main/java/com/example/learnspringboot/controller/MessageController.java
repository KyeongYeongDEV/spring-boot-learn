package com.example.learnspringboot.controller;

import com.example.learnspringboot.dto.MessageDto;
import com.example.learnspringboot.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kafka")
public class MessageController {
    private final KafkaProducerService kafkaProducerService;
    private final RedisService redisService;

    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageDto message) {
        kafkaProducerService.send(message);
        String result = "Kafka Message Sent Successfully";
        return result;
    }
}
