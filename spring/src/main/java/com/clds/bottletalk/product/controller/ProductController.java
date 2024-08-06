package com.clds.bottletalk.product.controller;


import com.clds.bottletalk.common.AWSCognitoService;
import com.clds.bottletalk.product.document.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/v1/api/products")
@Slf4j
public class ProductController {


    @Value("${elasticsearch.index-for-top5}")
    private String indexForTop5;

    @Value("${elasticsearch.index-for-products}")
    private String indexForProducts;

    private final AWSCognitoService awsCognito;
    private final RestHighLevelClient client;


    public ProductController(RestHighLevelClient client, AWSCognitoService awsCognitoService) {

        this.awsCognito = awsCognitoService;
        this.client = client;

    }

    @GetMapping("")
    public ResponseEntity<?> handleProducts(
            @RequestParam(required = false) String search,
            @RequestBody(required = false) UserDTO userDTO
    ) throws IOException {

        SearchRequest searchRequest = new SearchRequest(indexForProducts);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        if (search != null && !search.isEmpty()) {

            // 검색 키워드가 있을경우 실행
            WildcardQueryBuilder korWildcardQuery = QueryBuilders.wildcardQuery("kor_name", "*" + search + "*");
            WildcardQueryBuilder engWildcardQuery = QueryBuilders.wildcardQuery("eng_name", "*" + search + "*");
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .should(korWildcardQuery)
                    .should(engWildcardQuery);
            searchSourceBuilder.query(boolQuery);
        } else {
            // 검색 키워드 없을경우는 전체 조회
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        }

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 로그인정보가있고, 검색조건이 있을경우 로그를 생성한다.
        if (userDTO != null && search != null && !search.isEmpty()) {
            logUserSearch(userDTO, search, searchResponse);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()) {
            results.add(hit.getSourceAsMap());
        }

        return ResponseEntity.ok(results);

    }


    private void logUserSearch(UserDTO userDTO, String search, SearchResponse searchResponse){
        if (searchResponse.getHits().getTotalHits().value > 0) {
            String userId = userDTO.getUserId();
            Map<String, String> userInfo = awsCognito.getUserInfoFromCognito(userId);
            String userGender = awsCognito.parseGender(userInfo.get("gender"));
            String userBirthyear = awsCognito.parseBirthyear(userInfo.get("custom:BTbirthdate"));

            String searchJson = String.format(
                    "{ \"searchKeyword\" : \"%s\", \"email\": \"%s\", \"gender\": \"%s\", \"birthyear\": \"%s\" }",
                    search, userInfo.get("email"), userGender, userBirthyear
            );
            log.info(searchJson);
        }
    }




    @GetMapping("/top5Keywords")
    public ResponseEntity<?> getTop5Keywords() throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexForTop5);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.aggregation(AggregationBuilders.terms("top_keywords")
                .field("searchKeyword.keyword")
                .size(5));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        List<Map<String, Object>> topKeywords = new ArrayList<>();
        Terms terms = searchResponse.getAggregations().get("top_keywords");

        for (Terms.Bucket bucket : terms.getBuckets()) {
            Map<String, Object> keywordMap = new HashMap<>();
            keywordMap.put("keyword", bucket.getKeyAsString());
            keywordMap.put("count", bucket.getDocCount());
            topKeywords.add(keywordMap);
        }

        return ResponseEntity.ok(topKeywords);


    }
     /*
        우선 search  API를 사용할것이기 때문에 서치 리퀘스트를 생성한다.
        top5검색어를 사용하기 위해 aggregation 이 필요하였고 top*keywords 라는 aggregation 을 생성하였다.
        기준으로 사용할 필드는 searchKeyword 필드와 들어있는 값들 로 지정하였고,
        5가지 검색어를 받기위하여 사이즈는 5 로 설정하였다.
        이 응답 또한 받아온 응답에서 top.keywords 의 집계결과만 Terms 를 사용하여 필드의 고유값들을 그룹화 하였고,
        Bucket은 Terms의 집계 결과로 생성된 그룹을 나타낸다, 각 bucket은 키값과 카운트를 포함한다.
        따라서 bucket을 순회하면서  키값은 keyword 키의 밸류 로 지정하였고,
        count 키의 밸류 또한 count를 가져와 입력해주었다.
        그리고 가공된 데이터를 응답으로 보내주었다.
     */


    @GetMapping("/top5Products")
    public ResponseEntity<?> getTop5Products() throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexForTop5);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.aggregation(AggregationBuilders.terms("top_products")
                .field("id.keyword")
                .size(5)
                .subAggregation(
                        AggregationBuilders.topHits("top_hit")
                                .fetchSource(new String[]{"korName"}, null)
                ));

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        List<Map<String, Object>> topKeywords = new ArrayList<>();
        Terms terms = searchResponse.getAggregations().get("top_products");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            Map<String, Object> keywordMap = new HashMap<>();
            TopHits topHits = bucket.getAggregations().get("top_hit");
            SearchHit hit = topHits.getHits().getHits()[0];

            keywordMap.put("id", bucket.getKeyAsString());
            keywordMap.put("count", bucket.getDocCount());
            keywordMap.put("korName", hit.getSourceAsMap().get("korName"));
            topKeywords.add(keywordMap);
        }

        return ResponseEntity.ok(topKeywords);

    }
    /*
        검색어가 아닌 인기상품이기 때문에 가져와야 할 것이 카운트와 상품 이름 뿐만 아니라 상품의 id 값이 필요하다 생각하였다.
        따라서 생성하는 집계의 키 값을 id 로 지정하였고, 똑같이 5가지상품을 조회하게 사이즈를 지정한뒤
        추가적으로 상품의 이름이 보여야 한다 생각했다. 하지만 검색어를 가져왔던 방법과 달리 추가 속성을 가져오기 위해선 서브집합을
        만들어야하는 상황이었고 Terms 버킷 내에 top_hit 이라는 서브 집계를 추가하였다. 이 서브 집계는 korName 필드만 가져오도록 지정하였다.
        이렇게 설정한 요청으로 응답을 받아온후 현재 버킷의 top_hit 서브 집계 결과를 가져오고 그중 첫번째 히트를  가져온다.
        이후 키워드 맵에 하나씩 넣어주었다.
        기준키로 지정한 id 와 count 를 재외한 나머지 속성이 필요한 경우는 이렇게 서브집계를 만들어 hit를 사용하여 가져올수 있을것이다.
    */






}

