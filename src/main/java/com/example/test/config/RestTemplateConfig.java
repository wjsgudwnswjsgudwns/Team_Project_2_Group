package com.example.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // RestTemplate을 Bean으로 등록
    // 다른 클래스에서 RestTemplate을 주입받아 사용 가능
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}