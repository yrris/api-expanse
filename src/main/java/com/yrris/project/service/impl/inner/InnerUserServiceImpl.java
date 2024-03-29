package com.yrris.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yrris.project.common.ErrorCode;
import com.yrris.project.exception.ThrowUtils;
import com.yrris.project.mapper.UserMapper;
import com.yrris.project.model.entity.User;
import com.yrris.project.service.inner.InnerUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
@DubboService
public class InnerUserServiceImpl implements InnerUserService {
    @Resource
    private UserMapper userMapper;

    /**
     * 根据签名获取用户信息
     * @param accessKey 签名公钥
     * @return 用户信息 若不存在为null
     */
    @Override
    public User getCallingUser(String accessKey) {
        //参数校验
        ThrowUtils.throwIf(StringUtils.isBlank(accessKey), ErrorCode.PARAMS_ERROR);
        // 创建查询条件包装器
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey", accessKey);

        // 使用 UserMapper 的 selectOne 方法查询用户信息
        return userMapper.selectOne(queryWrapper);
    }
}
