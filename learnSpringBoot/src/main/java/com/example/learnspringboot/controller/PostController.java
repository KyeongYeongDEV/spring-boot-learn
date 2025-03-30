package com.example.learnspringboot.controller;

import com.example.learnspringboot.entity.Post;
import com.example.learnspringboot.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @GetMapping
    public List<Post> findAll() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id){
        return postService.getPopularPost(id);
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }
}
