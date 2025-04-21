# 초급

# ElasticSearch란?

> 도서관 사서 선생님이다
> 
- 책이 수천 권이 있어도 제목이나 작가 이름으로 `순식간에 찾아주는` 아주 똑똑한 사서이다
- 이 사서는  책을 어떻게 찾아야 알고 있어서, 우리가 물어보면 `“빨간 모자”` 이야기 중 제목에 빨간이 들어간 걸 금방 찾아준다.
- 이것이 ElasticSearch의 `검색 기능`이다

---

# 개념

`document` : 하나의 데이터 ⇒ 실제 데이터 객체 (예 : 게시글 하나)

`Index` : 책장  ⇒ 문서들을 저장하는 공간

`Field` : 문서의 속성 ⇒ 제목, 내용, 작성자 등

`Mapping` : 문서의 구조 정의서 ⇒ 설계도

`Query` :  사서에게 하는 질문 ⇒ 검색 조건

`Match` : Analyzer(분석기)를 통해 나눈 단어 기준으로 검색 ⇒ 자연어 검색

`Term` : 정확히 일치하는 값을 찾음 (분석 x) ⇒ 정확한 키워드 매칭

`Bool` : 여러 쿼리를 조합해서 조건 검색 

`must` : AND

`should` : OR (선택적으로 만족하면 점수 ↑)

`must_not` : 제외 조건

`filter` : 점수 영향 없이 조건 필터링

---

# API 정리

| 메서드 | URL | 설명 |
| --- | --- | --- |
| PUT / POST | `/{index}` | 인덱스 생성 |
| DELETE | `/{index}` | 인덱스 삭제 |
| POST | `/{index}/_doc` | 문서 추가(ID 자동 생성) |
| PUT | `/{index}/_doc/{id}` | 문서 추가 또는 수정 (ID 지정) |
| GET | `/{index}/_doc/{id}` | 특정 문서 조회 |
| DELETE | `/{index}/_doc/{id}` | 특정 문서 삭제 |
| GET / POST | `/{index}/_search` | 문서 검색 |
| GET | `/_cat/indices?v` | 모든 인덱스 보기 (인덱스 카탈로그) |
| GET | `/_cat/health?v` | 클러스터 상태 확인 |
| GET | `/{index}/_mapping` | 인데스 필드 구조 확인 |

---

# 실습

> 우리 게시글 Post를 ElasticSearch에 넣는다고 가정
> 

## 0. 도커 구성

- 현재 Kafka도 같이 사용하고 있기 때문에 같이 있다
- Elasticsearch 만 사용할 거면 Kibana와 Elasticsearch 설정만 하면 된다.

```java
version: '3'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka1:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka1
    container_name: kafka1
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENERS: PLAINTEXT://kafka1:29092,PLAINTEXT_HOST://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka1:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2
    depends_on:
      - zookeeper

  kafka2:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka2
    container_name: kafka2
    ports:
      - "9093:9093"
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENERS: PLAINTEXT://kafka2:29093,PLAINTEXT_HOST://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka2:29093,PLAINTEXT_HOST://localhost:9093
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2
    depends_on:
      - zookeeper

  kafka3:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka3
    container_name: kafka3
    ports:
      - "9094:9094"
    environment:
      KAFKA_BROKER_ID: 3
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENERS: PLAINTEXT://kafka3:29094,PLAINTEXT_HOST://0.0.0.0:9094
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka3:29094,PLAINTEXT_HOST://localhost:9094
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2
    depends_on:
      - zookeeper

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.17.9
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - ES_JAVA_OPTS=-Xms512m -Xmx512m
    ports:
      - "9200:9200"
    networks:
      - elk

  kibana:
    image: docker.elastic.co/kibana/kibana:7.17.9
    container_name: kibana
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - elk

networks:
  elk:
    driver: bridge

```

## 1. 의존성 추가 (build.gradle)

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
```

- gradle 업데이트 해주기

## 2. Post Entity를 Elasticsearch 문서로 등록하기

```java
package com.example.learnspringboot.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Document(indexName = "posts") // posts라는 인덱스에 저장됨
public class Post {

    @Id
    private Long id;
    private String title;
    private String content;
}
 
```

## 3. Elasticsearch 용 Repository 만들기

```java
package com.example.learnspringboot.repository;

import com.example.learnspringboot.entity.Post;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostElasticRepository extends ElasticsearchRepository<Post, Long> {
    // title 검색 예시도 가능해짐
}

```

## 4. Elasticsearch Service 만들기

```java
package com.example.learnspringboot.service;

import com.example.learnspringboot.entity.Post;
import com.example.learnspringboot.repository.PostElasticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final PostElasticRepository postElasticRepository;

    public void saveToElasticsearch(Post post) {
        postElasticRepository.save(post);
        System.out.println("🔍 Elasticsearch에 저장 완료: " + post.getTitle());
    }
}

```

## 5. PostService에서 저장시 Elasticsearch에도 저장

```java
public Post createPost(Post post) {
    Post savedPost = postRepository.save(post);
    kafkaProducerService.send(savedPost); // Kafka
    elasticsearchService.saveToElasticsearch(savedPost); // Elasticsearch 추가
    return savedPost;
}
```

# 테스트하기

## Kibana

1. 브라우저에서 [localhost:5601](http://localhost:5601) (Kibana 접속됨)

![image.png](%E1%84%8E%E1%85%A9%E1%84%80%E1%85%B3%E1%86%B8%201d181d8a13f080b49ccbf15f2b071d0c/image.png)

## 사용 예시1

1. 인덱스 생성

```java
PUT /posts
```

1. 문서 저장 (ID 자동 지정)

```java
POST /posts/_doc
Content-Type: application/json

{
  "title": "Elastic 배우기",
  "content": "쉽고 재미있다!"
}
```

1. 문서 저장 (ID 직접 지정 url이 다름)

```java
PUT /posts/_doc/1
Content-Type: application/json

{
  "title": "첫 번째 글",
  "content": "우리는 Elasticsearch를 배워요"
}

```

1. 문서 조회

```java
GET /posts/_doc/1
```

1. 문서 삭제

```java
DELETE /posts/_docs/1
```

1. 전체 문서 조회

```java
GET /posts/_search

{
  "query": {
    "match_all": {}
  }
}
```

1. 검색 쿼리

```java
GET /posts/_search

{
  "query": {
    "match": {
      "title": "Elastic"
    }
  }
}

```

## 사용 예시2

> match, term, bool을 이용해 검색하기
> 
- 예시 데이터 저장
    
    ```java
    POST http://localhost:9200/posts/_doc
    {
      "title": "엘라스틱서치는 놀라운 검색엔진입니다.",
      "content": "Elasticsearch는 로그 분석, 검색, 통계에 강력합니다."
    }
    ```
    

### Match - 단어를 분석해서 검색

```java
GET http://localhost:9200/posts/_search
{
  "query": {
    "match": {
      "title": "검색엔진"
    }
  }
}
```

- “검색엔진”은 내부적으로 “검색”+”엔진”+”입니다”으로 분석되어 찾아진다
    - 라고 예상을 했는데 검색이 안 된다
    - Nori분석기 또는 IK 분석기 같은 한글 전용 분석기를 사용해야 한다.
    아래에 계속..

### Term - 단어 전체가 정확히 일치할 때만 검색

```java
GET http://localhost:9200/posts/_search
{
  "query": {
    "term": {
      "title.keyword": "엘라스틱서치는 놀라운 검색엔진입니다."
    }
  }
}
```

- 결과
    - `title.keyword` 필드는 분석 없이 전체 문자열 기준 검색
    - “정확히 이 단어와 일치하는 것만 찾아줘” : "검색할 단어나 문장”

### Bool - 여러 조건을 동시에 줄 때

```java
GET http://localhost:9200/posts/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "title": "검색" } },
        { "match": { "content": "Elasticsearch" } }
      ],
      "must_not": [
        { "match": { "title": "느림" } }
      ]
    }
  }
}
```

---

# 형태소 분석

> Nori 분석기 기반 elasticsearch 인덱스 설계를 만들어보자
> 
- Nori Analyzer 설치를 해주자

```java
// Nori Analyzer 설치
docker exec -it [elasticsearch-container-name] bin/elasticsearch-plugin install analysis-nori
// 컨테이너 재실행
docker restart [elasticsearch-container-name]

```

## 1. 기존 인덱스가 있다면 삭제

```java
DELETE /posts
```

## 2. Nori 분석기 기반 인덱스 만들기

- `title`과 `content` 한국어 검색 최적화

```java
PUT /posts_kor
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_korean_analyzer": {
          "type": "custom",
          "tokenizer": "nori_tokenizer"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "my_korean_analyzer"
      },
      "content": {
        "type": "text",
        "analyzer": "my_korean_analyzer"
      }
    }
  }
}
```

## 3. 문서 저장(POST)

```java
POST /posts_kor/_doc
{
  "title": "검색엔진입니다",
  "content": "우리는 한국어 형태소 분석기로 검색 품질을 높입니다"
}
```

## 4. 검색(GET with match)

```java
GET /posts_kor/_search
{
  "query": {
    "match": {
      "title": "검색엔진"
    }
  }
}
```

## 5. 분석기 직접 테스트

```java
GET /posts_kor/_analyze
{
  "analyzer": "my_korean_analyzer",
  "text": "검색엔진입니다"
}
```

- 결과

```java
{
    "tokens": [
        {
            "token": "검색",
            "start_offset": 0,
            "end_offset": 2,
            "type": "word",
            "position": 0
        },
        {
            "token": "엔진",
            "start_offset": 2,
            "end_offset": 4,
            "type": "word",
            "position": 1
        },
        {
            "token": "이",
            "start_offset": 4,
            "end_offset": 7,
            "type": "word",
            "position": 2
        },
        {
            "token": "ᄇ니다",
            "start_offset": 4,
            "end_offset": 7,
            "type": "word",
            "position": 3
        }
    ]
}
```

- Nori 분석기가 `“검색엔진입니다”`를 위와 같이 분리를 했기 때문에 `“검색엔진”` 검색에 성공했다

# 요약

- 게시글을 책처럼 Elasticsearch라는 도서관에 저장한다.
- Kibana라는 서사한테 물어보면 금방 찾아준다.
- 실시간으로 저장하고, 빠르게 검색하는데 최고다

# 중급

# 고급 검색엔진 기능 구축

1. 자동완성 (Auto-complete) - `Edge NGram`
  - 사용자가 `“검”`만 쳐도 `“검색엔진”`이 추천되는 기능
2. 동의어 확장 (Synonym) - `“자동차”` → `“차”`, `“승용차”` 등 확장
3. Kibana 시각화 연동
  - 검색어 트렌드, 인기 키워드 등을 대시보드로 표현

## 1. 자동 완성 인덱스 만들기

```java
PUT /autocomplete_post
{
  "settings": {
    "analysis": {
      "analyzer": {
        "autocomplete_analyzer": {
          "tokenizer": "autocomplete_tokenizer"
        },
        "autocomplete_search_analyzer": {
          "tokenizer": "standard"
        }
      },
      "tokenizer": {
        "autocomplete_tokenizer": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 20,
          "token_chars": ["letter", "digit"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "autocomplete_analyzer",
        "search_analyzer": "autocomplete_search_analyzer"
      }
    }
  }
}
```

## 2. 문서 넣기

```java
POST /autocomplete_post/_doc
{
  "title": "검색엔진"
}
```

## 3. 자동완성 검색

- `“검”`이라고 검색했다고 가정

```java
GET /autocomplete_post/_search
{
  "query": {
    "match": {
      "title": "검"
    }
  }
}
```

- 결과

```java
{
    "took": 643,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 1,
            "relation": "eq"
        },
        "max_score": 0.2876821,
        "hits": [
            {
                "_index": "autocomplete_post",
                "_type": "_doc",
                "_id": "NjxkIpYBuHtZHrl3KkEy",
                "_score": 0.2876821,
                "_source": {
                    "title": "검색엔진"
                }
            }
        ]
    }
}
```

- `“검”`이라고 검색을 했지만 `“검색엔진”`이 잘 나온 것을 알 수 있다.

---

# 자동완성 UI 연동하기

## 1. 자동완성 분석기 세팅하기 (Custom Analyzer 만들기)

- 인덱스 매핑 정의 + edge_ngram 분석기 설정

```java
PUT /autocomplete_index
{
  "settings": {
    "analysis": {
      "tokenizer": {
        "autocomplete_tokenizer": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 20,
          "token_chars": ["letter", "digit"]
        }
      },
      "analyzer": {
        "autocomplete_analyzer": {
          "type": "custom",
          "tokenizer": "autocomplete_tokenizer",
          "filter": ["lowercase"]
        },
        "autocomplete_search_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "autocomplete_analyzer",
        "search_analyzer": "autocomplete_search_analyzer"
      }
    }
  }
}

```

- analyzer가 2개인 이유
  - `indexing` 할 때 → `edge_ngram`으로 자른다.
  - `검색`할 때 →일반 `standard analyzer`로 전체 단어 검색

## 2. 데이터 넣기 (자동완성용 title)

```java
POST /autocomplete_index/_doc
{
  "title": "검색엔진"
}

POST /autocomplete_index/_doc
{
  "title": "검색어 추천"
}

POST /autocomplete_index/_doc
{
  "title": "검색창 열기"
}
```

## 3. 검색어 prefix로 자동완성 테스트

```java
GET /autocomplete_index/_search
{
  "query": {
    "match": {
      "title": {
        "query": "검"
      }
    }
  }
}
```

- 결과 : `검색엔진`, `검색어 추천`, `검색어 열기` 모두 검색된다.

```java
{
    "took": 718,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 3,
            "relation": "eq"
        },
        "max_score": 0.14181954,
        "hits": [
            {
                "_index": "autocomplete_index",
                "_type": "_doc",
                "_id": "Nzx7IpYBuHtZHrl3RUGN",
                "_score": 0.14181954,
                "_source": {
                    "title": "검색엔진"
                }
            },
            {
                "_index": "autocomplete_index",
                "_type": "_doc",
                "_id": "ODx7IpYBuHtZHrl3bUGd",
                "_score": 0.12974027,
                "_source": {
                    "title": "검색어 추천"
                }
            },
            {
                "_index": "autocomplete_index",
                "_type": "_doc",
                "_id": "OTx7IpYBuHtZHrl3fUES",
                "_score": 0.12974027,
                "_source": {
                    "title": "검색어 열기"
                }
            }
        ]
    }
}
```

- 만약에 “색” 혹은 다른 단어로 검색을 하면 결과가 안 나온다.

```java
{
  "query": {
    "match": {
      "title": "색"
    }
  }
}
-- 결과
{
    "took": 2,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 0,
            "relation": "eq"
        },
        "max_score": null,
        "hits": []
    }
}
```

## 4. Spring Boot 검색 API 만들기

`SearchController.java`

⇒ 간단한 테스트를 위한 구현이기 때문에 conroller에서 다 구현

```java
package com.example.learnspringboot.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
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
}

```

## 5. Config들

Config 설정을 잘못하면 충돌이 일어난다.

- WebConfig

```java
package com.example.learnspringboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")  // 개발용으로 모든 origin 허용 (배포 시에는 도메인 지정)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
	    }
    }
```

- JpaConfig

```java
package com.example.learnspringboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.learnspringboot.repository.jpa"
)
public class JpaConfig {
}
```

- ElasticConfig

```java
package com.example.learnspringboot.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.example.learnspringboot.repository.elastic"
)
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(HttpHost.create(elasticsearchUrl)).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}

```

## 5. Html

![image.png](%E1%84%8C%E1%85%AE%E1%86%BC%E1%84%80%E1%85%B3%E1%86%B8%201d281d8a13f0800ca4ccd54d47ceae9e/image.png)

```java
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Elasticsearch 자동완성</title>
    <style>
        input {
            padding: 8px;
            font-size: 16px;
        }

        ul {
            list-style-type: none;
            padding-left: 0;
            margin-top: 8px;
            border: 1px solid #ccc;
            max-width: 250px;
        }

        li {
            padding: 6px;
            border-bottom: 1px solid #eee;
            cursor: pointer;
        }

        li:last-child {
            border-bottom: none;
        }

        li:hover {
            background-color: #f0f0f0;
        }
    </style>
</head>
<body>
<h2>자동완성 검색</h2>
<input type="text" id="searchInput" placeholder="제목 입력">
<ul id="resultList"></ul>

<script>
    let timeout = null;

    document.getElementById("searchInput").addEventListener("input", function () {
        clearTimeout(timeout);
        const query = this.value.trim();
        const list = document.getElementById("resultList");
        list.innerHTML = "";

        if (query.length < 1) {
            return; // 1글자 이상 입력 시만 검색
        }

        timeout = setTimeout(async () => {
            try {
                const res = await fetch(`/search/autocomplete?prefix=${encodeURIComponent(query)}`);
                const data = await res.json();

                if (!Array.isArray(data)) {
                    throw new Error("응답 형식 오류");
                }

                data.forEach(title => {
                    const li = document.createElement("li");
                    li.textContent = title;
                    li.onclick = () => {
                        document.getElementById("searchInput").value = title;
                        list.innerHTML = "";
                    };
                    list.appendChild(li);
                });
            } catch (e) {
                const li = document.createElement("li");
                li.textContent = "🔴 검색 중 오류 발생";
                li.style.color = "red";
                list.appendChild(li);
                console.error(e);
            }
        }, 300); // debounce: 300ms 입력 멈춤 후 요청
    });
</script>
</body>
</html>

```

# 하이라이팅 (Highlighting)

title 검색 시, 사용자가 입력한 키워드가 결과에서 강조되어 보이도록 만들기

- 우리가 브라우저에서  검색어를 입력하면 추천 검색어에 우리가 입력한 글자와 일치하는 추천 검색어는 진한 글씨체로 보이거나 다른색으로 표시해준다 이 기능을 구현한 것이다
- HOW?
  - 입력한 text는 <em> 태그가 붙어 강조효과가 적용이 된다.

예)

```java
입력: "김"
결과: "⟪<em>김</em>치볶음밥⟫", "⟪<em>김</em>밥천국⟫"
```

## 1. Controller 추가

```java
 @GetMapping("/highlight")
    public List<String> searchWithHighlight(@RequestParam String keyword) throws IOException {
        SearchResponse<Post> response = elasticsearchClient.search(s -> s
                        .index("posts")
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
```

## HTML 수정

```java
<input type="text" id="highlightInput" placeholder="검색어 입력">
<ul id="highlightResult"></ul>

<script>
    document.getElementById("highlightInput").addEventListener("input", async (e) => {
        const keyword = e.target.value;
        const res = await fetch(`/search/highlight?keyword=${keyword}`);
        const data = await res.json();

        const resultList = document.getElementById("highlightResult");
        resultList.innerHTML = "";

        data.forEach(text => {
            const li = document.createElement("li");
            li.innerHTML = text;  // ✨ highlight 부분 <em> 태그 적용
            resultList.appendChild(li);
        });
    });
</script>

```

## 결과

![image.png](%E1%84%8C%E1%85%AE%E1%86%BC%E1%84%80%E1%85%B3%E1%86%B8%201d281d8a13f0800ca4ccd54d47ceae9e/image%201.png)

- 현재는 하이라이팅을 적용을 했다
- 만약 다른 강조효과를 주고 싶으면 다른 태그를 적용하면 된다.

---

# 검색 결과 정렬

기본적으로 match 쿼리는 _score 순서 (= relevance 기반 ) 로 정렬된다.

하지만 필요에 따라 날짜순, 조회순, 알파벳순 등으로 정렬이 가능하다.

## 1. Entity 수정

- 우리는 최신순과 조회수순으로 정렬하는 것을 할 것이다

```java
package com.example.learnspringboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "posts")
public class Post {
    @Id
    private String id;
    private String title;
    private String content;
    private int views;
    private String createdAt;
}

```

- 기존 Elasticsearch Index는 삭제해야 한다.

```java
DELETE http://localhost:9200/autocomplete_index/
```

- 변경된 Entity에 맞게 새로운 인덱스를 생성한다.

```java
PUT http://localhost:9200/autocomplete_index

{
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword"
          }
        }
      },
      "views": {
        "type": "integer"
      },
      "createdAt": {
        "type": "date"
      },
      "content": {
        "type": "text"
      },
      "id": {
        "type": "keyword"
      }
    }
  }
}

```

## 2. Controller 추가

```java
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

```

## 3. HTML

```java
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>정렬 검색</title>
    <style>
        input, select {
            padding: 8px;
            margin: 4px;
        }
        ul {
            list-style-type: none;
            padding-left: 0;
        }
        li {
            padding: 6px;
        }
    </style>
</head>
<body>
<h2>정렬 기반 검색</h2>

<input id="keyword" placeholder="검색어 입력">
<select id="sortBy">
    <option value="title">제목(title)</option>
    <option value="views">조회수(views)</option>
    <option value="createdAt">작성일(createdAt)</option>
</select>
<select id="direction">
    <option value="asc">오름차순(asc)</option>
    <option value="desc">내림차순(desc)</option>
</select>
<button onclick="sortedSearch()">검색</button>

<ul id="resultList"></ul>

<script>
    async function sortedSearch() {
        const keyword = document.getElementById("keyword").value;
        const sortBy = document.getElementById("sortBy").value;
        const direction = document.getElementById("direction").value;
        const resultList = document.getElementById("resultList");
        resultList.innerHTML = "";

        try {
            const res = await fetch(`/search/sorted?keyword=${encodeURIComponent(keyword)}&sortBy=${sortBy}&direction=${direction}`);
            const data = await res.json();

            if (!Array.isArray(data)) {
                throw new Error("응답이 배열이 아님: " + JSON.stringify(data));
            }

            data.forEach(title => {
                const li = document.createElement("li");
                li.textContent = title;
                resultList.appendChild(li);
            });
        } catch (err) {
            const li = document.createElement("li");
            li.textContent = "🔴 검색 중 오류 발생";
            li.style.color = "red";
            resultList.appendChild(li);
            console.error(err);
        }
    }
</script>

</body>
</html>

```

## 결과

- 원하는대로 정렬이 되는 것을 확인할 수 있다

![image.png](%E1%84%8C%E1%85%AE%E1%86%BC%E1%84%80%E1%85%B3%E1%86%B8%201d281d8a13f0800ca4ccd54d47ceae9e/image%202.png)

![image.png](%E1%84%8C%E1%85%AE%E1%86%BC%E1%84%80%E1%85%B3%E1%86%B8%201d281d8a13f0800ca4ccd54d47ceae9e/image%203.png)

![image.png](%E1%84%8C%E1%85%AE%E1%86%BC%E1%84%80%E1%85%B3%E1%86%B8%201d281d8a13f0800ca4ccd54d47ceae9e/image%204.png)

![image.png](%E1%84%8C%E1%85%AE%E1%86%BC%E1%84%80%E1%85%B3%E1%86%B8%201d281d8a13f0800ca4ccd54d47ceae9e/image%205.png)

# 페이징 처리

## 1. Controller

```java
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
```

## 2. HTML

```java
<h2>페이징 검색</h2>
<input id="keyword" placeholder="검색어 입력">
<select id="sortBy">
    <option value="title">제목(title)</option>
    <option value="views">조회수(views)</option>
    <option value="createdAt">작성일(createdAt)</option>
</select>
<select id="direction">
    <option value="asc">오름차순(asc)</option>
    <option value="desc">내림차순(desc)</option>
</select>
<input id="page" type="number" value="0" min="0" style="width:60px" />
<input id="size" type="number" value="10" min="1" style="width:60px" />
<button onclick="pagedSearch()">검색</button>

<ul id="resultList"></ul>

<script>
    async function pagedSearch() {
        const keyword = document.getElementById("keyword").value;
        const sortBy = document.getElementById("sortBy").value;
        const direction = document.getElementById("direction").value;
        const page = document.getElementById("page").value;
        const size = document.getElementById("size").value;
        const resultList = document.getElementById("resultList");
        resultList.innerHTML = "";

        try {
            const res = await fetch(`/search/paged?keyword=${encodeURIComponent(keyword)}&sortBy=${sortBy}&direction=${direction}&page=${page}&size=${size}`);
            const data = await res.json();

            if (!Array.isArray(data)) {
                throw new Error("응답이 배열이 아님: " + JSON.stringify(data));
            }

            data.forEach(title => {
                const li = document.createElement("li");
                li.textContent = title;
                resultList.appendChild(li);
            });
        } catch (err) {
            const li = document.createElement("li");
            li.textContent = "🔴 검색 중 오류 발생";
            li.style.color = "red";
            resultList.appendChild(li);
            console.error(err);
        }
    }
</script>

```

# 다중 필드 검색

- 하나의 검색어로 `title`, `content` 두 필드를 동시에 검색하는 기능!

```java
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
```

# 가중치 검색 - boots 적용

- `multi_match` 검색시 `title` 필드에 더 높은 점수를 부여 → 검색 결과의 `우선순위`를 조절함

```java
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

```

- must : 검색 조건 (검색어)
- filter : 결과 제한 (조회수, 날짜 등)
- should : 점수 올려주는 조건 (선택적 boost)
- must_not  : 제외 조건

# 조회수 필터링

- 조회수가 일정 수치 이상인 문서만 검색

```java
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
                                                    .gt("100") // 조회수 100 초과 조건
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

```

# 날짜 필터링

- 문서의 createdAt 날짜가 최근 30일 이내인 경우만 검색
  - `range` 쿼리 사용
- `createdAt` 이 문자열이 아닌 date 포맷(yyyy-mm-dd)으로 인덱싱되어 있어야 함

```java
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
                                                    .gte(thirtyDaysAgo) // 최근 30일만
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

```

# 복합 조건 검색 API

1. `multi_match` : `title`과 `content` 동시 검색
2. `boost` : `title ^ 2` 가중치 적용
3. `조회수 필터` : `views` > 100
4. `날짜 필터` : 최근 30일 (`createdAt` ≥ `today` - 30일)
5. `정렬` : `views` 내림차순

```java
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
                                                    .gt("100") // 100 초과
                                            )
                                    )
                                    // 날짜 필터 (최근 30일 이내)
                                    .filter(f -> f
                                            .range(r -> r
                                                    .field("createdAt")
                                                    .gte(thirtyDaysAgo) // 30일 전부터 오늘까지
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

```

---

- 데이터 삽입

```
{
  "id": "1111",
  "title": "Elasticsearch Boost 적용하기",
  "content": "title 필드에 가중치를 설정해서 검색 정확도를 높인다",
  "views": 150,
  "createdAt": "2025-04-10"
}

```

```
{
  "id": "1112",
  "title": "Spring Data Elasticsearch 완전 정복",
  "content": "Elasticsearch에 대한 내용은 content에도 포함",
  "views": 90,
  "createdAt": "2025-03-05"
}

```

```java

{
  "id": "1113",
  "title": "인기 게시글 Top10",
  "content": "조회수가 높은 게시글만 모았습니다",
  "views": 500,
  "createdAt": "2025-03-20"
}

```

---

- 결과
  - `title 가중치`와 `최근 30일`에 작성된 게시글 그리고 `조회수`를 모두 비교해보았을 때 입력한 데이터 중 충족하는 하나의 결과가 나왔다

![image.png](%E1%84%8C%E1%85%AE%E1%86%BC%E1%84%80%E1%85%B3%E1%86%B8%201d281d8a13f0800ca4ccd54d47ceae9e/image%206.png)

# Query DSL 수동조립

- UI 필터 조건을 바탕으로 동적으로 쿼리를 조립할 수 있다.

## 왜 필요할까?

우선 수동 조립을 함으로서 사용자의 `필터 조건`에 따라 `쿼리를 조합`할 수 있다

원하는 대로 필터를 조립할 수 있기 때문에 `유연하고 복잡한 검색`을 구현할 수 있다.

## 사용 API

`co.elastic.clients.elasticsearch._types.query_dsl.*`

- Elasticsearch Java Client에서느 `Querybuilders` 같은 정적 빌더가 없기 때문에
  `람다 스타일` DSL을 활용해 직접 조립해야 한다.

---

## 예제 코드

```java
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
            .gte(JsonData.of(dateLimit))
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
    SearchResponse<Post> response = elasticsearchClient.search(s -> s
                    .index("autocomplete_index")
                    .query(finalQuery)
                    .sort(so -> so
                            .field(f -> f
                                    .field(sortBy)
                                    .order(sortOrder)
                            )
                    )
                    .size(10),
            Post.class
    );

    return response.hits().hits().stream()
            .map(hit -> hit.source().getTitle())
            .toList();
}
```

---

- 호출 예시

```java
GET http://localhost:8080/search/querydsl?keyword=스프링&minViews=150&daysAgo=10&sortBy=views&direction=desc
```

# 동적 쿼리 빌더 (UI 필터 → 쿼리 변환)

> 사용자 UI에서 넘어오는 조건을 Map<String, Object>로 받아,
자동으로 Elasticsearch 쿼리로 변환하는 구조
>

---

예시요청

```java
{
  "keyword": "스프링",
  "minViews": 100,
  "dateAfter": "2025-03-10",
  "sortBy": "views",
  "direction": "desc"
}
```

## 1. DTO 정의

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String keyword;
    private Integer minViews;
    private String dateAfter; // ISO date 형식 문자열
    private String sortBy;
    private String direction;
}
```

## 2. Controller 구현

```java
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

```

---

테스트 예시

```java
POST http://localhost:8080/search/dynamic

{
  "keyword": "Spring",
  "minViews": 120,
  "dateAfter": "2025-03-15",
  "sortBy": "views",
  "direction": "desc"
}
```

---

결과

### 수동 조립과 동적 쿼리 빌더의 차이점

`수동 조립` → 개발자가 `정해진 조건`을 기준으로 Query DSL을 직접 코드로 조립

- 조건이 고정 : 검색어, 필터, 정렬 모두 개발자가 정함
- 구조가 명확 : 쿼리 조합 흐름이 코드로 잘 보임
- 단순 사용에 적합 : 검색 조건이 변하지 않을 때는 깔끔하게 사용 가능
- 유연성 낮음 : 조건이 많아지면 코드가 복잡해짐

`동적 빌더` → 사용자(UI/프론트)에서 `조건을 다양하게 반영` 가능하도록 동적으로 쿼리 생성

- 조건 유동적 : 사용자 입력에 따라 쿼리가 조립됨
- 유연함 : 필터, 정렬, 페이징 등 모든 조건을 선택적으로 적용
- 실무형 : 프론트의 체크박스/드롭다운 조건을 받아서 처리 가능
- 단점 : 코드가 다소 길고, 잘못 만들면 NullPointer 등 오류 위험

> 언제 무엇을 사용해야 할까?
>
> - 조건은 고정, 쿼리 직접 쓰고 싶다 ⇒ 수동 조립
> - 검색창 + 필터 + 정렬 조건이 UI에서 날아온다 ⇒ 동적 쿼리 빌더

| 기능 | 방식 | 이유 |
| --- | --- | --- |
| 단순 검색 (title 포함) | 수동 조립 | 딱 정해진 조건만 있음 |
| 필터링 검색 (가격, 날짜, 태그 등 UI 기반) | 동적 빌더 | 조건이 바뀌니까 유연하게 받아야 함 |
| 관리자 페이지에서 다중 조건 검색 | 동적 빌더 | 프론트가 조건을 다양하게 던짐 |
| 인기 게시물 (조회수 내림차순) | 수동 조립 | 조건 고정됨 |

# 자동완성 강화 : Edge NGram

기존 `match` 기반 자동완성은 `“엘라”` 입력시 `“엘라스틱”`만 찾는다
`”라스”`로는 검색이 불가능 했다.

- `“엘”`, `“엘라”`, `“라스틱”`, `“스틱서”` 등 가능하게 하려면?
  - `Edge NGram` 분석기 설정이 필요하다

## Edge NGram vs NGram

| 구분 | `Edge NGram` | `NGram` |
| --- | --- | --- |
| 🌱목적 | **접두사** 기반 자동완성 | **중간 문자 포함 검색** |
| 예시 입력 | `"Spring"` | `"Spring"` |
| 토큰 결과 | `S`, `Sp`, `Spr`, `Spri`, `Sprin`, `Spring` | `S`, `Sp`, `pri`, `rin`, `ing`, `Sprin`, `ring`, `Spring` |
| 용도 | 자동완성 (prefix) 검색 | 전체 문자 검색, OCR/검색 키워드 유사도 |
| 단점 | 접두사만 인식 (`중간 단어 불가`) | 너무 많은 토큰 → 인덱싱/검색 `느려짐` |
- 결론
  - 자동완성 ⇒ **Edge NGram**
  - 오타 검색, 비정형 텍스트 검색 ⇒ **NGram**

## 포퍼먼스 최적화시 주의할 점

- Edge NGram은 상당히 유용하지만, 잘못 쓰면 성능이 떨어질 수 있다.
- `min_gram` 너무 작으면
  - "S", "E"처럼 **단일 문자 검색** 가능 → 결과 폭발
- `max_gram` 너무 크면
  - 긴 단어 하나로 **수십 개 토큰** 생성됨 → 인덱싱 과부하
- 전체 필드에 적용 시
  - 10개 필드 × 1,000개 문서 = 수만 개 토큰 → 성능 이슈 발생
- 검색어 하이라이트
  - 토큰이 너무 많아 highlight 정밀도 떨어짐

### 추천 세팅

```java
"min_gram": 2,
"max_gram": 15
```

- 너무 짧은 토큰 방지
- 실질적 자동완성 키워드 범위 확보

## prefix 쿼리 vs ngram analyzer 비교

| 항목 | `prefix` 쿼리 | `Edge NGram` 분석기 |
| --- | --- | --- |
| 분석기 사용 여부 | ❌ 아님 | ✅ 인덱싱 시 분석 |
| 필드 타입 필요 | `keyword` | `text` |
| 정확도 | 높음 (정확한 prefix) | 보통 (prefix 추출 기반) |
| 퍼포먼스 | 빠름 | 느림 (토큰이 많음) |
| 자동완성 유연성 | 낮음 (한 방향만) | 높음 (조합 다양) |
- 단어 전체 중 일부만 자동완성(ex. “라스틱”) ⇒ Edge NGram
- 엄격하게 접두사만 검색(ex. “Spr) ⇒ prefix 쿼리 + keyword 필드

---

## 1. 인덱스 정의

```java
PUT http://localhost:9200/autocomplete_ngram_index
```

```java
{
  "settings": {
    "analysis": {
      "tokenizer": {
        "edge_ngram_tokenizer": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 20,
          "token_chars": [ "letter", "digit" ]
        }
      },
      "analyzer": {
        "autocomplete_analyzer": {
          "type": "custom",
          "tokenizer": "edge_ngram_tokenizer",
          "filter": [ "lowercase" ]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "autocomplete_analyzer",
        "search_analyzer": "standard"
      },
      "content": { "type": "text" },
      "views": { "type": "integer" },
      "createdAt": { "type": "date", "format": "yyyy-MM-dd" }
    }
  }
}
```

- `token_chars : [ "letter", "digit" ]`
  - 글자와 숫자만 인식
- `type : custom`
  - 사용자 정의 분석기 생성
- `tokenizer`
  - 위에서 정의한 `edge_ngram_tokenizer` 사용
- `filter : lowercase`
  - 토큰을 전부 소문자로 변화하여 검색 일치률 향상
- `search_analyzer : standard`
  - 검색시 일반적인 `analyzer(standard)`로 검색어 분석

---

- 데이터 삽입

```java
{
  "title": "Spring Boot Elasticsearch 실전 활용",
  "content": "Elasticsearch를 Spring Boot 프로젝트에 적용하는 방법",
  "views": 150,
  "createdAt": "2025-04-10"
}
```

```java
{
  "title": "Elasticsearch 검색 속도 튜닝 가이드",
  "content": "search_after, scroll 등을 활용한 고속 검색",
  "views": 130,
  "createdAt": "2025-03-20"
}
```

```java
{
  "title": "Spring Aggregation 분석",
  "content": "terms, avg, max 집계 쿼리를 설명합니다.",
  "views": 200,
  "createdAt": "2025-04-01"
}
```

```java
{
  "title": "Elastic Mapping 오류 해결",
  "content": "date, keyword 타입 오류 해결법",
  "views": 90,
  "createdAt": "2025-02-25"
}
```

```java
{
  "title": "Spring Boot Query DSL 적용법",
  "content": "bool, must, filter를 조합한 검색 API 만들기",
  "views": 300,
  "createdAt": "2025-04-15"
}
```

---

## 2. Controller 정의

```java
@GetMapping("/autocomplete")
public List<String> autocomplete(@RequestParam String prefix) throws IOException {
    SearchResponse<Post> response = elasticsearchClient.search(s -> s
                    .index("autocomplete_index")
                    .query(q -> q
                            .match(m -> m
                                    .field("title")
                                    .query(prefix)
                            )
                    )
                    .size(10),
            Post.class
    );

    return response.hits().hits().stream()
            .map(hit -> hit.source().getTitle())
            .toList();
}

```

---

## 3. 검색 예시

```java
GET /search/autocomplete?prefix=라스
```

- `“엘라스틱서치 튜토리얼”`도 검색됨

# 한글 초성 자동완선

- elasticsearch는 기본적으로 한글 초성 검색이 안 된다.
  - ex. ㅅㅂㅇ → “스프링 부트 엘라스틱” 이렇게 불가능

## 해결 전략

### 방법 1 : 초설 필드 직접 만들기

- `title` → `chosung` 필드로 초성만 추출 (데이터 전처리시)
  - ex. “스프링” → “ㅅㅍㄹ”
- 사용자가 “ㅅㅂㅇ” 입력 - > chosung 필드로 검색

---

구현

```java
public String getChosung(String text) {
    // 결과를 담을 StringBuilder 생성
    StringBuilder sb = new StringBuilder();

    // 입력된 문자열을 한 글자(char)씩 순회
    for (char ch : text.toCharArray()) {

        // 한글 유니코드 범위인지 확인 (가~힣)
        if (ch >= 0xAC00 && ch <= 0xD7A3) {

            // 한글 유니코드의 시작점(0xAC00)을 기준으로 현재 글자의 상대 위치 계산
            int base = ch - 0xAC00;

            /*
             * 한글의 구성 방식은 다음과 같습니다:
             * 하나의 글자는 = 초성(19) * 중성(21) * 종성(28) 조합
             * base를 (21 * 28)으로 나누면 초성 인덱스를 구할 수 있음
             */
            int chosungIndex = base / (21 * 28);

            // 초성 리스트에서 해당 인덱스의 초성 문자 추출
            char chosungChar = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ".charAt(chosungIndex);

            // 결과에 초성 추가
            sb.append(chosungChar);
        }

        // 한글이 아닌 경우는 무시됨 (필요 시 else로 로직 추가 가능)
        else {
			    sb.append(ch); // 한글이 아니면 그대로 추가
				}

    }

    // 초성으로 구성된 문자열 반환
    return sb.toString();
}

```

### 방법2 : 한글 전용 분석기 (Hangul Decomposer Plugin)

- Elasticsearch plugin 사용
- 형태소 분석기 + 초성 분리 기능 내장
- 설치 필요 ( 복잡도 상승)

## 인덱스 수정

```java
{
  "settings": {
    "analysis": {
      "tokenizer": {
        "edge_ngram_tokenizer": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 20,
          "token_chars": [ "letter", "digit" ]
        }
      },
      "analyzer": {
        "autocomplete_analyzer": {
          "type": "custom",
          "tokenizer": "edge_ngram_tokenizer",
          "filter": [ "lowercase" ]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "autocomplete_analyzer",
        "search_analyzer": "standard"
      },
      "chosung": {
        "type": "text",
        "analyzer": "autocomplete_analyzer",
        "search_analyzer": "standard"
      },
      "content": {
        "type": "text"
      },
      "views": {
        "type": "integer"
      },
      "createdAt": {
        "type": "date",
        "format": "yyyy-MM-dd"
      }
    }
  }
}

```

## 1. Post Entity 수정 -  초성 필드 추가

```java
@Document(indexName = "autocomplete_ngram_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    private String id;

    private String title;
    private String content;
    private int views;
    private String createdAt;

    private String chosung;  // ✅ 초성 필드 추가
}
```

## 2. 초성 추출 유틸 메서드

```java
public class HangulUtils {
    private static final String CHOSUNG_LIST = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";

    public static String extractChosung(String text) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch >= 0xAC00 && ch <= 0xD7A3) {
                int base = ch - 0xAC00;
                int chosungIndex = base / (21 * 28);
                sb.append(CHOSUNG_LIST.charAt(chosungIndex));
            }
        }
        return sb.toString();
    }
}

```

## 3. 문서 저장시 초성 자동 생성

- 게시물을 저장할 때 title → chosung 자동 세팅

```java
@PostMapping("/posts")
public ResponseEntity<String> createPost(@RequestBody Post post) throws IOException {
    String chosung = HangulUtils.extractChosung(post.getTitle());
    post.setChosung(chosung);

    IndexResponse response = elasticsearchClient.index(i -> i
            .index("autocomplete_ngram_index")
            .id(post.getId())
            .document(post)
    );

    return ResponseEntity.ok("저장 완료 ID: " + response.id());
}

```

## 4. Controller

```java
@GetMapping("/autocomplete/chosung")
public List<String> autocompleteByChosung(@RequestParam String prefix) throws IOException {
    SearchResponse<Post> response = elasticsearchClient.search(s -> s
                    .index("autocomplete_ngram_index")
                    .query(q -> q
                            .match(m -> m
                                    .field("chosung")
                                    .query(prefix)
                            )
                    )
                    .size(10),
            Post.class
    );

    return response.hits().hits().stream()
            .map(hit -> hit.source().getTitle())
            .toList();
}

```


