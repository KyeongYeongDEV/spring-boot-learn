package com.example.learnspringboot.service;

import com.example.learnspringboot.entity.Post;
import com.example.learnspringboot.repository.PostElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElasticsearchService{
    private final PostElasticsearchRepository postElasticsearchRepository;

    public void saveToElasticsearch(Post post){
        postElasticsearchRepository.save(post);
        System.out.println("Elasticsearch post saved!!" + post.getTitle());
    }
}
