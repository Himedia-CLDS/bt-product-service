package com.clds.bottletalk.product.controller;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.clds.bottletalk.common.CognitoAttributeParser;
import com.clds.bottletalk.common.ResponseDTO;

import com.clds.bottletalk.product.document.UserDTO;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.clds.bottletalk.common.CognitoAttributeParser.*;

@RestController
@RequestMapping("/v1/api/products")
@Slf4j
public class ProductController {

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${Elasticsearch.INDEX}")
    private String index;


    private final AWSCognitoIdentityProvider cognitoClient;



    private final CognitoAttributeParser cognitoAttributeParser;

    private final RestHighLevelClient client;


    public ProductController(AWSCognitoIdentityProvider cognitoClient, CognitoAttributeParser cognitoAttributeParser, RestHighLevelClient client) {

        this.cognitoClient = cognitoClient;
        this.cognitoAttributeParser = cognitoAttributeParser;
        this.client = client;

    }



//    @GetMapping("")
//    public ResponseEntity<?> allProducts(){
//
//
//
//
//
//        return ResponseEntity.ok(null);
//    }





    @GetMapping("")
    public ResponseEntity<List<Map<String, Object>>> searchProducts(@RequestParam String search, @RequestBody UserDTO userDTO) {
        try {
            String userId = userDTO.getUserId();
            Map<String, String> userInfo = getUserInfoFromCognito(userId);
            String userGender = parseGender(userInfo.get("gender"));
            String userBirthyear = parseBirthyear(userInfo.get("custom:BTbirthdate"));


            SearchRequest searchRequest = new SearchRequest("products");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            WildcardQueryBuilder korWildcardQuery = QueryBuilders.wildcardQuery("kor_name", "*" + search + "*");
            WildcardQueryBuilder engWildcardQuery = QueryBuilders.wildcardQuery("eng_name", "*" + search + "*");

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .should(korWildcardQuery)
                    .should(engWildcardQuery);

            searchSourceBuilder.query(boolQuery);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

           if (searchResponse.getHits().getTotalHits().value > 0) {
               String searchJson = String.format(
                       "{ \"searchKeyword\" : \"%s\", \"email\": \"%s\", \"gender\": \"%s\", \"birthyear\": \"%s\" }",
                       search, userInfo.get("email"), userGender, userBirthyear
               );
               log.info(searchJson);
           }


            List<Map<String, Object>> results = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits()) {
                results.add(hit.getSourceAsMap());
            }

            return ResponseEntity.ok(results);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/top5Products")
    public ResponseEntity<?> getTop5Products() throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.aggregation(AggregationBuilders.terms("top_keywords")
                .field("id.keyword")
                .size(5)
                .subAggregation(
                        AggregationBuilders.topHits("top_hit")
                                .size(1)
                                .fetchSource(new String[]{"korName"}, null)
                ));

        searchRequest.source(searchSourceBuilder);


        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        List<Map<String, Object>> topKeywords = new ArrayList<>();
        Terms terms = searchResponse.getAggregations().get("top_keywords");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            Map<String, Object> keywordMap = new HashMap<>();
            TopHits topHits = bucket.getAggregations().get("top_hit");
            SearchHit hit = topHits.getHits().getHits()[0];

            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String korName = sourceAsMap.get("korName").toString();

            keywordMap.put("id", bucket.getKeyAsString());
            keywordMap.put("count", bucket.getDocCount());
            keywordMap.put("korName", korName);
            topKeywords.add(keywordMap);
        }

        return ResponseEntity.ok(topKeywords);

    }


    @GetMapping("/top5Keywords")
    public ResponseEntity<List<Map<String, Object>>> getTop5Keywords() throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
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



    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable("productId") String productId) {
        try {
            GetRequest getRequest = new GetRequest("products", productId);
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

            if (getResponse.isExists()) {


                return ResponseEntity.ok().body(getResponse);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(HttpStatus.NOT_FOUND, "제품을 찾을 수 없습니다.", null));
            }
        } catch (Exception e) {
            log.error("제품 조회 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", null));
        }
    }


    private Map<String, String> getUserInfoFromCognito(String userId) {
        AdminGetUserRequest userRequest = new AdminGetUserRequest().withUserPoolId(userPoolId).withUsername(userId);

        AdminGetUserResult userResult = cognitoClient.adminGetUser(userRequest);

        Map<String, String> userAttributes = new HashMap<>();
        for (AttributeType attribute : userResult.getUserAttributes()) {
            userAttributes.put(attribute.getName(), attribute.getValue());
        }

        return userAttributes;

    }


}
