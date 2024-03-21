package com.yrris.exampleinterface.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.yrris.exampleinterface.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

/**
 * 调用第三方的接口的客户端
 */
@Slf4j
public class ApiExpanseClient {

    //GET请求
    public String getNameByGet(String name) {
        //传入请求参数，拼接在url中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name",name);
        //使用HttpUtil工具发起GET请求
        String result = HttpUtil.get("http://localhost:8081/api/name/",paramMap);
        //打印结果
        log.info(result);
        return result;
    }

    //POST请求
    public String getNameByPost(@RequestParam String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name",name);
        String result  = HttpUtil.post("http://localhost:8081/api/name/",paramMap);
        log.info(result);
        return result;
    }

    //JSON类型 Restful请求
    public String getUserNameByPost(@RequestBody User user) {
        //将User对象转化为JSON格式字符串
        String json = JSONUtil.toJsonStr(user);
        HttpResponse httpResponse = HttpRequest.post("http://localhost:8081/api/name/user")
                .body(json) //将JSON字符串设置为请求体
                .execute(); //执行
        String result = httpResponse.body();
        //打印服务器返回结果
        log.info(result);
        return result;
    }
}
