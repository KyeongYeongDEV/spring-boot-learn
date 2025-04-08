package com.example.learnspringboot.service;

import com.example.learnspringboot.entity.Post;
import com.example.learnspringboot.service.KafkaProducerService;
import com.example.learnspringboot.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RedisTemplate<String, Object> redisTemplate;

    public Post createPost(Post post) {
        Post savedPost = postRepository.save(post);
        kafkaProducerService.send(savedPost); // Kafka에 발행
        return savedPost;
    }

    public String getPost(Long postId) {
        String key = "post_" + postId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            System.out.println("✅ Redis 조회 성공");
            return cached.toString();
        }

        System.out.println("🔄 Redis 미스 → DB 조회");
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            redisTemplate.opsForValue().set(key, postOpt.get(), 60, TimeUnit.SECONDS);
            return postOpt.get().toString();
        }

        throw new IllegalArgumentException("Post not found");
    }
}
