package com.yrris.exampleinterface;

import com.yrris.exampleinterface.client.ApiExpanseClient;
import com.yrris.exampleinterface.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
            String accessKey = "root";
            String secretKey = "123456";
            ApiExpanseClient client = new ApiExpanseClient(accessKey,secretKey);
            String result1 = client.getNameByGet("java");
            String result2 = client.getNameByPost("go");
            User user = new User();
            user.setUserName("yrris");
            String result3 = client.getUserNameByPost(user);
            log.info(result1);
            log.info(result2);
            log.info(result3);
    }
}
