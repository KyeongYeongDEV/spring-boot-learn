package com.example.learnspringboot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "posts")
public class Post {
    @Id
    private String id;
    private String title;
    private String content;
    private int views;
    private LocalDateTime createdAt;
}
