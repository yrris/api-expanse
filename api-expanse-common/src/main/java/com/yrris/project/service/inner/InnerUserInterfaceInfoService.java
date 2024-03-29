package com.yrris.project.service.inner;

public interface InnerUserInterfaceInfoService {

    /**
     * 调用接口次数统计
     *
     * @param userId          用户id
     * @param interfaceInfoId 接口id
     * @return
     */
    boolean callCount(long userId, long interfaceInfoId);
}
