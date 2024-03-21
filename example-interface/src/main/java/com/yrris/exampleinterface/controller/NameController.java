package com.yrris.exampleinterface.controller;

import com.yrris.apiexpansesdk.model.User;
import com.yrris.apiexpansesdk.utils.SignUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/")
    public String getNameByGet(String name) {
        return "GET 你的名字是" + name;
    }

    @PostMapping("/")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request) {
        // 从请求头中获取参数
        String accessKey = request.getHeader("accessKey");
        String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String sign = request.getHeader("sign");
        String body = request.getHeader("body");

        // 校验
        // todo 在数据库中查询是否分配给用户
        if (!accessKey.equals("root")) {
            throw new RuntimeException("无权限!");

        }
        // todo 校验随机数 使用 hashMap 或者 redis
        // 简答判断逻辑应小于4位
        if (Long.parseLong(nonce) > 10000) {
            throw new RuntimeException("无权限!");

        }
        // 时间戳与当前时间差应该小于5min
        if (System.currentTimeMillis() - Long.parseLong(timestamp) > 5 * 60 * 1000) {
            throw new RuntimeException("当前签名时间过期!");

        }

        // todo 从数据库中通过获取到的 accessKey 查询出 secretKey
        String serverSign = SignUtil.genSign(body, "123456");
        if (!sign.equals(serverSign)) {
            throw new RuntimeException("无权限!");
        }
        // 如果权限校验通过，返回 "POST 用户名字是" + 用户名
        return "POST 用户名字是" + user.getUserName();
    }

}

