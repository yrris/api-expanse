package com.yrris.exampleinterface;

import com.yrris.apiexpansesdk.client.ApiExpanseClient;
import com.yrris.apiexpansesdk.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
@Slf4j
@SpringBootTest
class ExampleInterfaceApplicationTests {

	// 注入apiExpanseClient的Bean
	@Resource
	private ApiExpanseClient apiExpanseClient;
	@Test
	void contextLoads() {
		String result1 = apiExpanseClient.getNameByGet("java");
		String result2 = apiExpanseClient.getNameByPost("go");
		User user = new User();
		user.setUserName("yrris");
		String result3 = apiExpanseClient.getUserNameByPost(user);
		// 打印结果
		log.info(result1);
		log.info(result2);
		log.info(result3);
	}

}
