package com.example.learnspringboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.learnspringboot.repository.elastic")
public class ElasticsearchConfig {
}
