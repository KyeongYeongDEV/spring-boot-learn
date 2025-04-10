package com.example.learnspringboot.repository.elastic;

import com.example.learnspringboot.entity.Post;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostElasticsearchRepository extends ElasticsearchRepository<Post, Long> {
}
