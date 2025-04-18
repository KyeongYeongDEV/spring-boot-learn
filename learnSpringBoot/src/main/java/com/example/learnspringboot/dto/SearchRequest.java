package com.example.learnspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String keyword;
    private Integer minViews;
    private String dateAfter;
    private String sortBy;
    private String direction;
}
