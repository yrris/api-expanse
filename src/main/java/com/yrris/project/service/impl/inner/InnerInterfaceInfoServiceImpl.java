package com.yrris.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yrris.project.common.ErrorCode;
import com.yrris.project.exception.ThrowUtils;
import com.yrris.project.mapper.InterfaceInfoMapper;
import com.yrris.project.model.entity.InterfaceInfo;
import com.yrris.project.service.inner.InnerInterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    /**
     * 根据请求路径、方法、参数获取内部接口信息
     *
     * @param path   请求URL
     * @param method 请求方法
     * @param params 请求参数
     * @return 接口信息，如果找不到匹配的接口返回 null
     */
    @Override
    public InterfaceInfo getCallingInterfaceInfo(String path, String method, String params) {
        //参数校验
        ThrowUtils.throwIf(StringUtils.isAnyBlank(path,method), ErrorCode.PARAMS_ERROR);
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        //校验请求路径、请求方法
        queryWrapper.eq("url",path);
        queryWrapper.eq("method",method);
        // todo 校验请求参数
//        if(StringUtils.isNotBlank(params)){
//            queryWrapper.eq("url",path);
//        }
        return interfaceInfoMapper.selectOne(queryWrapper);
    }
}
