package com.example.learnspringboot.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import com.example.learnspringboot.dto.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.learnspringboot.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/multifield")
    public List<String> multiFieldSearch(
            @RequestParam String keyword
    ) throws IOException {
        // Elasticsearch multi_match 쿼리
        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("autocomplete_index") // 사용 중인 인덱스 이름
                        .query(q -> q
                                .multiMatch(m -> m
                                        .query(keyword)                         // 사용자가 입력한 검색어
                                        .fields("title", "content")             // 동시에 검색할 필드들
                                )
                        )
                        .size(10),
                Post.class
        );

        // 검색 결과에서 title만 리스트로 추출
        return response.hits().hits().stream()
                .map(hit -> hit.source().getTitle())
                .toList();
    }
    @GetMapping("/boost")
    public List<String> boostedSearch(@RequestParam String keyword) throws IOException {
        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("autocomplete_index")
                        .query(q -> q
                                .multiMatch(m -> m
                                        .query(keyword)
                                        .fields("title^3", "content") // title에 3배 가중치 적용
                                )
                        )
                        .size(10),
                Post.class
        );

        return response.hits().hits().stream()
                .map(hit -> hit.source().getTitle())
                .toList();
    }

    @GetMapping("/popular")
    public List<String> popularSearch(@RequestParam String keyword) throws IOException {
        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("autocomplete_index")
                        .query(q -> q
                                .bool(b -> b
                                        .must(m -> m
                                                .multiMatch(mm -> mm
                                                        .query(keyword)
                                                        .fields("title", "content")
                                                )
                                        )
                                        .filter(f -> f
                                                .range(r -> r
                                                        .field("views")
                                                        .gt(JsonData.fromJson("100")) // 조회수 100 초과 조건
                                                )
                                        )
                                )
                        )
                        .size(10),
                Post.class
        );

        return response.hits().hits().stream()
                .map(hit -> hit.source().getTitle())
                .toList();
    }

    @GetMapping("/recent")
    public List<String> recentSearch(@RequestParam String keyword) throws IOException {
        // 오늘 날짜 기준으로 30일 전 날짜 계산
        String thirtyDaysAgo = java.time.LocalDate.now().minusDays(30).toString(); // 예: "2024-03-15"

        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("autocomplete_index")
                        .query(q -> q
                                .bool(b -> b
                                        .must(m -> m
                                                .multiMatch(mm -> mm
                                                        .query(keyword)
                                                        .fields("title", "content")
                                                )
                                        )
                                        .filter(f -> f
                                                .range(r -> r
                                                        .field("createdAt")
                                                        .gte(JsonData.fromJson(thirtyDaysAgo)) // 최근 30일만
                                                )
                                        )
                                )
                        )
                        .size(10),
                Post.class
        );

        return response.hits().hits().stream()
                .map(hit -> hit.source().getTitle())
                .toList();
    }
    @GetMapping("/complex")
    public List<String> complexSearch(
            @RequestParam String keyword
    ) throws IOException {
        // 최근 30일 기준 날짜 계산
        String thirtyDaysAgo = java.time.LocalDate.now().minusDays(30).toString();

        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("autocomplete_index")
                        .query(q -> q
                                .bool(b -> b
                                        // 필수 검색어 조건 (multi_match + boost)
                                        .must(m -> m
                                                .multiMatch(mm -> mm
                                                        .query(keyword)
                                                        .fields("title^2", "content") // title에 가중치 2
                                                )
                                        )
                                        // 조회수 필터
                                        .filter(f -> f
                                                .range(r -> r
                                                        .field("views")
                                                        .gt(JsonData.fromJson("100")) // 100 초과
                                                )
                                        )
                                        // 날짜 필터 (최근 30일 이내)
                                        .filter(f -> f
                                                .range(r -> r
                                                        .field("createdAt")
                                                        .gte(JsonData.fromJson(thirtyDaysAgo)) // 30일 전부터 오늘까지
                                                )
                                        )
                                )
                        )
                        // 조회수 기준 내림차순 정렬
                        .sort(so -> so
                                .field(f -> f
                                        .field("views")
                                        .order(SortOrder.Desc)
                                )
                        )
                        .size(10),
                Post.class
        );

        // 결과에서 title 필드만 추출하여 반환
        return response.hits().hits().stream()
                .map(hit -> hit.source().getTitle())
                .toList();
    }

    @GetMapping("/querydsl")
    public List<String> queryDslSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "100") int minViews,
            @RequestParam(defaultValue = "30") int daysAgo,
            @RequestParam(defaultValue = "views") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) throws IOException {
        // 30일 전 날짜
        String dateLimit = LocalDate.now().minusDays(daysAgo).toString();

        // 검색어 조건 (multi_match + boost)
        Query keywordQuery = MultiMatchQuery.of(mm -> mm
                .query(keyword)
                .fields("title^2", "content")
        )._toQuery();

        // 조회수 조건
        Query viewsFilter = RangeQuery.of(r -> r
                .field("views")
                .gt(JsonData.of(minViews))
        )._toQuery();

        // 날짜 조건
        Query dateFilter = RangeQuery.of(r -> r
                .field("createdAt")
                .gte(JsonData.fromJson(dateLimit))
        )._toQuery();

        // bool 조합
        Query finalQuery = BoolQuery.of(b -> b
                .must(keywordQuery)
                .filter(viewsFilter)
                .filter(dateFilter)
        )._toQuery();

        // 정렬 방향
        SortOrder sortOrder = "asc".equalsIgnoreCase(direction) ? SortOrder.Asc : SortOrder.Desc;

        // 최종 검색 요청
        SearchResponse<Post> response = elasticsearchClient.search(s->s
                .index("autocomplete_index")
                .query(finalQuery)
                .sort(so -> so
                        .field(f -> f
                                .field(sortBy)
                                .order(sortOrder)
                        )
                ).size(10),
                Post.class
        );

        List<String> result = response.hits().hits().stream()
                .map(hit -> hit.source().getTitle())
                .toList();

        return result;
    }

    @PostMapping("/dynamic")
    public List<String> dynamicQuery(@RequestBody SearchRequest req) throws IOException {
        List<Query> filters = new ArrayList<>();

        // 검색어 조건
        Query keywordQuery = MultiMatchQuery.of(mm -> mm
                .query(req.getKeyword())
                .fields("title^2", "content")
        )._toQuery();

        // 조회수 조건 (옵션)
        if (req.getMinViews() != null) {
            filters.add(RangeQuery.of(r -> r
                    .field("views")
                    .gt(JsonData.of(req.getMinViews()))
            )._toQuery());
        }

        // 날짜 필터 (옵션)
        if (req.getDateAfter() != null) {
            filters.add(RangeQuery.of(r -> r
                    .field("createdAt")
                    .gte(JsonData.of(req.getDateAfter()))
            )._toQuery());
        }

        // 전체 조합
        Query finalQuery = BoolQuery.of(b -> b
                .must(keywordQuery)
                .filter(filters)
        )._toQuery();

        // 정렬
        SortOrder order = "asc".equalsIgnoreCase(req.getDirection()) ? SortOrder.Asc : SortOrder.Desc;

        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("autocomplete_index")
                        .query(finalQuery)
                        .sort(so -> so
                                .field(f -> f
                                        .field(Optional.ofNullable(req.getSortBy()).orElse("views"))
                                        .order(order)
                                )
                        )
                        .size(10),
                Post.class
        );

        return response.hits().hits().stream()
                .map(hit -> hit.source().getTitle())
                .toList();
    }

}
