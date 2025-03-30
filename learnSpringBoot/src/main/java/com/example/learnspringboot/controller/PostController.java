package com.example.learnspringboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;


@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController { // DB 사용 x, Redis만 사용함

    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/{postId}/{ttlInSeconds}") // 어노테이션에 파람 변수를 적어줘야 한다 // 아니면 @PathVariable 대신 @RequestParam을 달고 쿼리스트링 형식으로 보내줘야 한다.
    public String savePostToRedis(@PathVariable Long postId, @PathVariable Long ttlInSeconds) {
        String key = "post_" + postId; // Reids key 설정
        String value = "Redis에서 저장한 게시글 ID : " + postId; //Redis Value 설정

        redisTemplate.opsForValue().set(key, value, ttlInSeconds, TimeUnit.SECONDS); // Redis에 TTL을 포함해서 저장

        return "게시글 캐시 저장 완료 (TTL: " + ttlInSeconds + "초)";
    }

    @GetMapping("/{postId}")
    public String getPostFromRedis(@PathVariable Long postId) {
        String key = "post_" + postId;
        Object value = redisTemplate.opsForValue().get(key); // Redis에서 key값을 이용해서 값을 검색한다.

        if (value != null) {
            System.out.println("Redis에서 조회");
            return value.toString();
        } else {
            return "캐시에 값이 없음";
        }
    }

    @DeleteMapping("/{postId}")
    public String deletePostFromRedis(@PathVariable Long postId) {
        String key = "post_" + postId;
        redisTemplate.delete(key);
        return "캐시에서 삭제 완료";
    }
}
