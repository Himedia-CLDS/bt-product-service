package com.clds.bottletalk.config;


import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean       // 빈으로 등록해 사용할 때마다 만들지 않고 불러오게 하기위해 bean으로 설정
    public ModelMapper modelMapper() {

        /* 설정 정보 추가 */
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);     // 필드매칭 변환

        return modelMapper;
    }





}
