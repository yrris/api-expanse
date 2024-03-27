package com.yrris.apigateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    // 创建路由规则的构建器
//	@Bean
//	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//		// 创建路由规则的构建器
//		return builder.routes()
//				// 定义路由规则，给该规则起一个名字 "tobaidu"
//				.route("tonima", r -> r.path("/nima")
//						// 将满足 "/baidu" 路径的请求转发到 "https://www.baidu.com"
//						.uri("http://localhost:8101/api/doc.html"))
//				// 定义路由规则，给该规则起一个名字 "toyupiicu"
//				.route("togoogle", r -> r.path("/google")
//						// 将满足 "/google" 路径的请求转发到 "http://google.icu"
//						.uri("http://yupi.icu"))
//				// 创建并返回路由规则配置对象
//				.build();
//	}
}
