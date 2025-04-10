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

`Bool` : 여러 쿼리를 조합해서 조건 검색 ⇒ `AND`, `OR`, `NOT`

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

## 사용 예시

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

---

# 예제

> 우리 게시글 Post를 ElasticSearch에 넣는다고 가정
> 

## 예시 데이터

```json
{
  "id": 1,
  "title": "엘라스틱서치는 너무 빠르다",
  "content": "카프카랑 같이 써보니까 진짜 실시간 검색이 되네"
}
```

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

## Elasticsearch

1. 테스트 데이터 삽입 (`Post /_doc`)

![image.png](%E1%84%8E%E1%85%A9%E1%84%80%E1%85%B3%E1%86%B8%201d181d8a13f080b49ccbf15f2b071d0c/image.png)

1. 조회 (`Get /_search`)

![image.png](%E1%84%8E%E1%85%A9%E1%84%80%E1%85%B3%E1%86%B8%201d181d8a13f080b49ccbf15f2b071d0c/image%201.png)

## Kibana

1. 브라우저에서 [localhost:5601](http://localhost:5601) (Kibana 접속)

![image.png](%E1%84%8E%E1%85%A9%E1%84%80%E1%85%B3%E1%86%B8%201d181d8a13f080b49ccbf15f2b071d0c/image%202.png)

# 요약

- 게시글을 책처럼 Elasticsearch라는 도서관에 저장한다.
- Kibana라는 서사한테 물어보면 금방 찾아준다.
- 실시간으로 저장하고, 빠르게 검색하는데 최고다
