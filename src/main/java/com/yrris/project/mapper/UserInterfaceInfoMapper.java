package com.yrris.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yrris.project.model.entity.UserInterfaceInfo;

import java.util.List;

public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    List<UserInterfaceInfo> listTopNumInterfaceInfo(int limit);
}