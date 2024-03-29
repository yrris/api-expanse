package com.yrris.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yrris.project.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;
import com.yrris.project.model.entity.UserInterfaceInfo;

public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo,boolean add);

    QueryWrapper<UserInterfaceInfo> getQueryWrapper(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest);

    /**
     * 调用接口次数统计
     * @param userId 用户id
     * @param interfaceInfoId 接口id
     * @return
     */
    boolean callCount(long userId ,long interfaceInfoId);
}
