package com.yrris.project.service.inner;

import com.yrris.project.model.entity.InterfaceInfo;

public interface InnerInterfaceInfoService {

    /**
     * 从数据库中查询被调用接口是否存在
     *
     * @param path   请求路径
     * @param method 请求方法
     * @param params 请求参数
     * @return 接口信息
     */
    InterfaceInfo getCallingInterfaceInfo(String path, String method, String params);
}
