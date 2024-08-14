package com.clds.bottletalk.common;

import com.clds.bottletalk.model.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WebClientService {

    private final WebClient webClient;

    public WebClientService(WebClient webClient) {
        this.webClient = webClient;
    }


    public Boolean hasKey(String userId) {
        String response = webClient.get()
                .uri("/redis/hasKey?userId=" + userId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return response.equals("true");
    }


    public Boolean setKey(UserDTO userDTO) {
        System.out.println("setKey 요청 보내기 직전 : " + userDTO.toString());
        String response = webClient.post()
                .uri("/redis/setKey")
                .bodyValue(userDTO)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return response.equals("true");
    }


    public UserDTO getKey(String userId) {
        return webClient.get()
                .uri("/redis/getKey?userId=" + userId)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }


}