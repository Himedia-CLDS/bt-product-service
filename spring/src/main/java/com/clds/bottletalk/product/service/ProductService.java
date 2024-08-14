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
