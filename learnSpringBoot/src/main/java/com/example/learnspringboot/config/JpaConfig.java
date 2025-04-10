package com.example.learnspringboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.learnspringboot.repository.jpa")
public class JpaConfig {
}
