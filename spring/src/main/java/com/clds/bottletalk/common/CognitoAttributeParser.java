package com.clds.bottletalk.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class CognitoAttributeParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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