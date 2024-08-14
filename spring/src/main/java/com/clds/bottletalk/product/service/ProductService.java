package com.clds.bottletalk.product.service;


import com.clds.bottletalk.common.AWSCognitoService;
import com.clds.bottletalk.model.UserDTO;
import com.clds.bottletalk.common.WebClientService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.clds.bottletalk.common.AWSCognitoService.objectMapper;

@Slf4j
@Service
public class ProductService {


    @Value("${elasticsearch.index-for-products}")
    private String indexForProducts;

    @Value("${elasticsearch.index-for-top5}")
    private String indexForTop5;

    private final AWSCognitoService awsCognitoService;
    private final RestHighLevelClient client;
    private final WebClientService webClientService;


    public ProductService(RestHighLevelClient client, AWSCognitoService awsCognitoService,WebClientService webClientService ) {
        this.awsCognitoService = awsCognitoService;
        this.client = client;
        this.webClientService = webClientService;
    }


    public ResponseEntity<?> getProduct(String productId, String userId) throws IOException {
        GetRequest getRequest = new GetRequest(indexForProducts, productId);
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> map = getResponse.getSourceAsMap();
        if (userId != null) {

            if (!webClientService.hasKey(userId)) {
                Map<String, String> userInfo = awsCognitoService.getUserInfoFromCognito(userId);
                String gender = awsCognitoService.parseGender(userInfo.get("gender"));
                String birthYear = awsCognitoService.parseBirthyear(userInfo.get("custom:BTbirthdate"));
                map.put("gender", gender);
                map.put("birthYear", birthYear);
                String mapForLog = objectMapper.writeValueAsString(map);
                log.info(mapForLog);
                UserDTO userDTO = new UserDTO(userId,gender,birthYear);
                System.out.println(userDTO);
                webClientService.setKey(userDTO);

            } else {

                UserDTO userInfo = webClientService.getKey(userId);
                map.put("gender", userInfo.getGender());
                map.put("birthYear", userInfo.getBirthYear());
                String mapForLog = objectMapper.writeValueAsString(map);
                log.info(mapForLog);
            }
        }
        return ResponseEntity.ok(map);
    }


    public ResponseEntity<?> getTop5Products(String userId) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexForTop5);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        if (userId != null) {
            if (webClientService.hasKey(userId)) {
                UserDTO userInfo = webClientService.getKey(userId);
                searchSourceBuilder.query(QueryBuilders.termQuery("gender", userInfo.getGender()));

            } else {
                Map<String, String> userInfo = awsCognitoService.getUserInfoFromCognito(userId);
                String gender = awsCognitoService.parseGender(userInfo.get("gender"));
                String birthYear = awsCognitoService.parseBirthyear(userInfo.get("birthYear"));
                webClientService.setKey(new UserDTO(userId, gender, birthYear));
                searchSourceBuilder.query(QueryBuilders.termQuery("gender", gender));
            }
        }

        searchSourceBuilder.aggregation(AggregationBuilders.terms("top_products")
                .field("product_id.keyword")
                .size(5)
                .subAggregation(
                        AggregationBuilders.topHits("top_hit")
                                .fetchSource(new String[]{"kor_name"}, null)
                ));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        List<Map<String, Object>> top5Products = new ArrayList<>();
        Terms terms = searchResponse.getAggregations().get("top_products");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            Map<String, Object> keywordMap = new HashMap<>();
            TopHits topHits = bucket.getAggregations().get("top_hit");
            SearchHit hit = topHits.getHits().getHits()[0];
            keywordMap.put("id", bucket.getKeyAsString());
            keywordMap.put("count", bucket.getDocCount());
            keywordMap.put("kor_name", hit.getSourceAsMap().get("kor_name"));
            top5Products.add(keywordMap);
        }

        return ResponseEntity.ok(top5Products);

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


    public ResponseEntity<?> getTop5Keywords(String userId) throws IOException {

        SearchRequest searchRequest = new SearchRequest(indexForTop5);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        if (userId != null) {
            if (webClientService.hasKey(userId)) {
                UserDTO userInfo = webClientService.getKey(userId);
                searchSourceBuilder.query(QueryBuilders.termQuery("gender", userInfo.getGender()));

            } else {
                Map<String, String> userInfo = awsCognitoService.getUserInfoFromCognito(userId);
                String gender = awsCognitoService.parseGender(userInfo.get("gender"));
                String birthYear = awsCognitoService.parseBirthyear(userInfo.get("birthYear"));
                webClientService.setKey(new UserDTO(userId, gender, birthYear));
                searchSourceBuilder.query(QueryBuilders.termQuery("gender", gender));
            }
        }

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
       검색어가 아닌 인기상품이기 때문에 가져와야 할 것이 카운트와 상품 이름 뿐만 아니라 상품의 id 값이 필요하다 생각하였다.
       따라서 생성하는 집계의 키 값을 id 로 지정하였고, 똑같이 5가지상품을 조회하게 사이즈를 지정한뒤
       추가적으로 상품의 이름이 보여야 한다 생각했다. 하지만 검색어를 가져왔던 방법과 달리 추가 속성을 가져오기 위해선 서브집합을
       만들어야하는 상황이었고 Terms 버킷 내에 top_hit 이라는 서브 집계를 추가하였다. 이 서브 집계는 korName 필드만 가져오도록 지정하였다.
       이렇게 설정한 요청으로 응답을 받아온후 현재 버킷의 top_hit 서브 집계 결과를 가져오고 그중 첫번째 히트를  가져온다.
       이후 키워드 맵에 하나씩 넣어주었다.
       기준키로 지정한 id 와 count 를 재외한 나머지 속성이 필요한 경우는 이렇게 서브집계를 만들어 hit를 사용하여 가져올수 있을것이다.
   */
    public ResponseEntity<?> getAllProducts() throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexForProducts);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(500);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()) {
            results.add(hit.getSourceAsMap());
        }
        return ResponseEntity.ok(results);
    }


    public ResponseEntity<?> searchProducts(String search, String userId) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexForProducts);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder korWildcardQuery = QueryBuilders.wildcardQuery("kor_name", "*" + search + "*");
        QueryBuilder engWildcardQuery = QueryBuilders.wildcardQuery("eng_name", "*" + search + "*");
        QueryBuilder boolQuery = QueryBuilders.boolQuery()
                .should(korWildcardQuery)
                .should(engWildcardQuery);
        searchSourceBuilder.query(boolQuery);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (userId != null) {
            logUserSearch(userId, search, searchResponse);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()) {
            results.add(hit.getSourceAsMap());
        }

        return ResponseEntity.ok(results);
    }


    private void logUserSearch(String userId, String search, SearchResponse searchResponse) {
        if (searchResponse.getHits().getTotalHits().value > 0) {
            if (!webClientService.hasKey(userId)) {
                Map<String, String> userInfo = awsCognitoService.getUserInfoFromCognito(userId);
                String userGender = awsCognitoService.parseGender(userInfo.get("gender"));
                String userBirthyear = awsCognitoService.parseBirthyear(userInfo.get("custom:BTbirthdate"));
                String searchJson = String.format(
                        "{ \"searchKeyword\" : \"%s\", \"gender\": \"%s\", \"birthYear\": \"%s\" }",
                        search, userGender, userBirthyear
                );
               webClientService.setKey(new UserDTO(userId, userGender, userBirthyear));
                log.info(searchJson);
            } else {
                System.out.println("레디스 사용!");
                UserDTO userInfo = webClientService.getKey(userId);
                String searchJson = String.format(
                        "{ \"searchKeyword\" : \"%s\", \"gender\": \"%s\", \"birthYear\": \"%s\" }",
                        search, userInfo.getGender(), userInfo.getBirthYear()
                );
                log.info(searchJson);
            }

        }
    }










}
