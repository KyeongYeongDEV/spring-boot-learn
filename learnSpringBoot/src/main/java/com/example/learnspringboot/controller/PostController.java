package com.example.learnspringboot.controller;

import com.example.learnspringboot.entity.Post;
import com.example.learnspringboot.service.KafkaProducerService;
import com.example.learnspringboot.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final KafkaProducerService kafkaProducerService;

//    // 전체 게시글 조회
//    @GetMapping
//    public List<Post> getAllPosts() {
//        return postService.getAllPosts();
//    }

    // 단건 게시글 조회 (Redis 캐시 전략 적용됨)
    @GetMapping("/{postId}")
    public String getPost(@PathVariable Long postId) {
        return postService.getPost(postId);
    }
//
//    @PostMapping("/kafka")
//    public String testKafka(@RequestBody Map<String, String> payload) {
//        String message = payload.get("message");
//        kafkaProducerService.send(message);
//        return "메세지 전송 완료";
//    }

    // 게시글 생성
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }

//    // 캐시 삭제 (수정, 삭제 등 반영 시 사용)
//    @DeleteMapping("/{postId}/cache")
//    public String clearPostCache(@PathVariable Long postId) {
//        postService.invalidatePostCache(postId);
//        return "캐시 삭제 완료";
//    }
}
