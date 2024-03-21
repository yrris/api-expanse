package com.yrris.apiexpanse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yrris.apiexpanse.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.yrris.apiexpanse.model.entity.InterfaceInfo;
import com.yrris.apiexpanse.model.vo.InterfaceInfoVO;

import javax.servlet.http.HttpServletRequest;

public interface InterfaceInfoService extends IService<InterfaceInfo>{
    void validInterfaceInfo(InterfaceInfo interfaceInfo,boolean add);

    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request);

    Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);
}
