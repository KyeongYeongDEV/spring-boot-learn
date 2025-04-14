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
                        .index("autocomplete_index")
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

    @GetMapping("/paged")
    public List<String> pagedSearch(
            @RequestParam String keyword,                      // 검색할 키워드 (title 필드 기준)
            @RequestParam(defaultValue = "0") int page,        // 현재 페이지 번호 (0부터 시작)
            @RequestParam(defaultValue = "10") int size,       // 한 페이지에 보여줄 문서 수
            @RequestParam(defaultValue = "createdAt") String sortBy, // 정렬할 필드 (예: createdAt, title 등)
            @RequestParam(defaultValue = "desc") String direction     // 정렬 방향 (asc or desc)
    ) throws IOException {

        // Elasticsearch에서 사용할 from 값 계산 (페이지 * 사이즈)
        int from = page * size;

        // Elasticsearch 검색 요청 수행
        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("autocomplete_index")         // 검색 대상 인덱스 (실제 인덱스명 사용!)
                        .from(from)                          // 몇 번째부터 시작할지 지정 (페이징)
                        .size(size)                          // 한 번에 가져올 문서 수
                        .query(q -> q                        // 검색 쿼리 정의
                                .match(m -> m
                                        .field("title")      // title 필드를 기준으로
                                        .query(keyword)      // 사용자가 입력한 키워드로 match 검색
                                )
                        )
                        .sort(so -> so                       // 정렬 조건 정의
                                .field(f -> f
                                        .field(sortBy)       // 사용자가 선택한 필드로 정렬
                                        .order("asc".equalsIgnoreCase(direction) // 정렬 방향 설정
                                                ? SortOrder.Asc
                                                : SortOrder.Desc)
                                )
                        ),
                Post.class // 결과를 매핑할 클래스 지정
        );

        // 검색 결과에서 Post 객체의 title 필드만 추출하여 반환
        List<String> results = response.hits().hits().stream()
                .map(hit -> hit.source().getTitle())
                .toList();

        return results;
    }


}
