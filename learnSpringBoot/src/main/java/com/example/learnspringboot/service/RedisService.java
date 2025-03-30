package com.example.learnspringboot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveData(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String setData(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }
}
