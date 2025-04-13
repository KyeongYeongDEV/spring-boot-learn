package com.example.learnspringboot.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.FieldType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.learnspringboot.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ElasticsearchClient elasticsearchClient;

    @GetMapping("/autocomplete")
    public List<String> autocomplete(@RequestParam String prefix) throws IOException {
        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("autocomplete_index") // 꼭 Postman에 맞춰야 함!
                        .query(q -> q
                                .match(m -> m
                                        .field("title")
                                        .query(prefix)
                                )
                        )
                        .size(10),
                Post.class
        );

        return response.hits().hits()
                .stream()
                .map(hit -> hit.source().getTitle())
                .toList();
    }

    @GetMapping("/highlight")
    public List<String> searchWithHighlight(@RequestParam String keyword) throws IOException {
        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("autocomplete_index")
                        .query(q -> q
                                .match(m -> m
                                        .field("title")
                                        .query(keyword)
                                )
                        )
                        .highlight(h -> h
                                .fields("title", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                        ),
                Post.class
        );

        List<String> results = new ArrayList<>();
        for (Hit<Post> hit : response.hits().hits()) {
            String highlighted;             // fallback

            // highlight된 부분 사용
            if (hit.highlight().get("title") != null) {
                highlighted = hit.highlight().get("title").get(0);
            } else {
                assert hit.source() != null;
                highlighted = hit.source().getTitle();
            }
            results.add(highlighted);
        }

        return results;
    }

    @GetMapping("/sorted")
    public List<String> sortedSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) throws IOException {

        // Elasticsearch 정렬 쿼리 실행
        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("autocomplete_index") // ✅ 반드시 존재하는 인덱스
                        .query(q -> q
                                .match(m -> m
                                        .field("title")
                                        .query(keyword)
                                )
                        )
                        .sort(so -> so
                                .field(f -> f
                                        .field(sortBy)
                                        .order("asc".equalsIgnoreCase(direction)
                                                ? SortOrder.Asc
                                                : SortOrder.Desc)
                                )
                        )
                        .size(10),
                Post.class
        );

        // 결과 반환: title 목록
        return response.hits().hits().stream()
                .map(hit -> hit.source().getTitle())
                .toList();
    }



}
