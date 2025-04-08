package com.example.learnspringboot.config;

import com.example.learnspringboot.entity.Post;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    // ProducerFactory 설정
    @Bean
    public ProducerFactory<String, Post> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Kafka 서버 주소
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");

        // Key/Value 직렬화 방식 설정
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // value type을 json으로 변경 => String 형이 아니라 JSON형으로 줄 거기 때문

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // KafkaTemplate 등록
    @Bean
    public KafkaTemplate<String, Post> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
