package com.yrris.project.service.inner;

import com.yrris.project.model.entity.User;

/**
 * 用户服务
 */
public interface InnerUserService {

    /**
     * 获取是否分配给用户过认证签名
     *
     * @param accessKey 签名公钥
     * @return 用户
     */
    User getCallingUser(String accessKey);
}
