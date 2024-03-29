package com.yrris.project.service.impl.inner;

import com.yrris.project.service.UserInterfaceInfoService;
import com.yrris.project.service.inner.InnerUserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public boolean callCount(long userId, long interfaceInfoId) {
        return userInterfaceInfoService.callCount(userId,interfaceInfoId);
    }
}
