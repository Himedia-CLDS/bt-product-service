package com.clds.bottletalk.common;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class AWSCognitoService {


    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;
    private final AWSCognitoIdentityProvider cognitoClient;
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public AWSCognitoService(AWSCognitoIdentityProvider cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public Map<String, String> getUserInfoFromCognito(String userId) {
        AdminGetUserRequest userRequest = new AdminGetUserRequest().withUserPoolId(userPoolId).withUsername(userId);
        AdminGetUserResult userResult = cognitoClient.adminGetUser(userRequest);
        Map<String, String> userAttributes = new HashMap<>();
        for (AttributeType attribute : userResult.getUserAttributes()) {
            userAttributes.put(attribute.getName(), attribute.getValue());
        }
        return userAttributes;
    }


    public static String parseGender(String encodedGender) {
        try {
            String decodedGender = URLDecoder.decode(encodedGender, StandardCharsets.UTF_8.toString());
            JsonNode genderNode = objectMapper.readTree(decodedGender);
            return genderNode.get(0).get("value").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }


    public static String parseBirthyear(String encodedBirthdate) {
        try {
            String decodedBirthdate = URLDecoder.decode(encodedBirthdate, StandardCharsets.UTF_8.toString());
            JsonNode birthdateNode = objectMapper.readTree(decodedBirthdate);
            JsonNode dateNode = birthdateNode.get(0).get("date");
            return String.valueOf(dateNode.get("year").asInt());
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}
