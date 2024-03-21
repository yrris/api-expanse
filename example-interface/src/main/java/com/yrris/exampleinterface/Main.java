package com.yrris.exampleinterface;

import com.yrris.exampleinterface.client.ApiExpanseClient;
import com.yrris.exampleinterface.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
            ApiExpanseClient yuApiClient = new ApiExpanseClient();
            String result1 = yuApiClient.getNameByGet("java");
            String result2 = yuApiClient.getNameByPost("go");
            User user = new User();
            user.setUserName("yrris");
            String result3 = yuApiClient.getUserNameByPost(user);
            log.info(result1);
            log.info(result2);
            log.info(result3);
    }
}
