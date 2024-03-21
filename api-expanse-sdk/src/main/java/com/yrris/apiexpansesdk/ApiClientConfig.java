package com.yrris.apiexpansesdk;

import com.yrris.apiexpansesdk.client.ApiExpanseClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

// 配置类
@Configuration
// 为配置加上前缀为"api-expanse.client"
@ConfigurationProperties("api-expanse.client")
@Data
// 自动扫描组件
@ComponentScan
public class ApiClientConfig {

    private String accessKey;

    private String secretKey;
    @Bean
    public ApiExpanseClient apiExpanseClient(){
        return new ApiExpanseClient(accessKey,secretKey);
    }
}
