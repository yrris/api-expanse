package com.yrris.apiexpanse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrris.apiexpanse.common.ErrorCode;
import com.yrris.apiexpanse.constant.CommonConstant;
import com.yrris.apiexpanse.exception.BusinessException;
import com.yrris.apiexpanse.exception.ThrowUtils;
import com.yrris.apiexpanse.mapper.UserInterfaceInfoMapper;
import com.yrris.apiexpanse.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;
import com.yrris.apiexpanse.model.entity.UserInterfaceInfo;
import com.yrris.apiexpanse.service.UserInterfaceInfoService;
import com.yrris.apiexpanse.utils.SqlUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo> implements UserInterfaceInfoService {

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是添加操作，检查要开通的调用接口id 和用户id 是否存在以及 开通时设置的次数是否合理
        if (add) {
            Long userId = userInterfaceInfo.getUserId();
            Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
            Integer leftNum = userInterfaceInfo.getLeftNum();
            if (userId <= 0 || interfaceInfoId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "开通的接口或者用户不存在!");
            }
            if (leftNum < 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "分配的调用次数不能小于 0 !");
            }
        }
    }

    @Override
    public QueryWrapper<UserInterfaceInfo> getQueryWrapper(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (userInterfaceInfoQueryRequest == null) {
            return queryWrapper;
        }
        Long id = userInterfaceInfoQueryRequest.getId();
        Long userId = userInterfaceInfoQueryRequest.getUserId();
        Long interfaceInfoId = userInterfaceInfoQueryRequest.getInterfaceInfoId();
//        Integer totalNum = userInterfaceInfoQueryRequest.getTotalNum();
//        Integer leftNum = userInterfaceInfoQueryRequest.getLeftNum();
        Integer status = userInterfaceInfoQueryRequest.getStatus();
        String sortField = userInterfaceInfoQueryRequest.getSortField();
        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(interfaceInfoId), "interfaceInfoId", interfaceInfoId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.orderBy(SqlUtil.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public boolean callCount(long userId, long interfaceInfoId) {
        //没有验证用户id或者接口是否存在，若不存在不会更新
        ThrowUtils.throwIf(userId<=0 || interfaceInfoId<=0,ErrorCode.PARAMS_ERROR);
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId",userId);
        updateWrapper.eq("interfaceInfoId",interfaceInfoId);
        updateWrapper.setSql("totalNum = totalNum + 1,leftNum = leftNum -1");
        return this.update(updateWrapper);
    }
}
