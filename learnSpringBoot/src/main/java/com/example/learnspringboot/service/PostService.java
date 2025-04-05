package com.example.learnspringboot.service;

import com.example.learnspringboot.entity.Post;
import com.example.learnspringboot.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final RedisService redisService;
    private final KafkaProducerService kafkaProducerService;

    public Post createPost(Post post) {
        Post savedPost = postRepository.save(post);;
        kafkaProducerService.sendMessage("게시글 등록됨 : ID = " + savedPost.getId());

        return savedPost;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }


    public String getPost(Long postId){
        String redisKey = "post_" + postId;

        // Redis 먼저 조회
        String cachedValue = redisService.getData(redisKey);
        if(cachedValue != null) {
            System.out.println("Redis에서 조회됨");
            return cachedValue;
        }
        //없으면 디비 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        String value = "DB에서 조회된 게시글 : " + post.getId() + " / " + post.getId();

        redisService.setDataWithTTL(redisKey,value,60);
        return value;
    }

    public void invalidatePostCache(Long postId){
        String redisKey = "post_" + postId;
        redisService.deleteData(redisKey);
    }
}
