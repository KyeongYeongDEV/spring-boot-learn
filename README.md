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
