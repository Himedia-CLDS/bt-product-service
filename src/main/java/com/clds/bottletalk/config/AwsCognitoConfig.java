package com.clds.bottletalk.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsCognitoConfig {

    @Value("${aws.access-key-cognito}")
    private String accessKey;

    @Value("${aws.secret-key-cognito}")
    private String secretKey;

    @Value("${aws.cognito.region}")
    private String region;

    @Bean
    public AWSCognitoIdentityProvider cognitoClient() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        return AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(region)
                .build();
    }
}
