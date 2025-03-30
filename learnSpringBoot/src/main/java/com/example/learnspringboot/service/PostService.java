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

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    @Cacheable(value = "popularPosts", key = "#postId")
    public String getPopularPost(Long postId) {
        System.out.println("DB에서 게시글 조회");
        String message = "인기 게시글 Id : " + postId;
        return message;
    }

    @CacheEvict(value = "popularPosts", key = "#postId")
    public void invalidateCahce(Long postId) {
        System.out.println("캐시 삭제됨");
    }
}
