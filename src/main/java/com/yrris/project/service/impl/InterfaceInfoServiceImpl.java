package com.yrris.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrris.project.common.ErrorCode;
import com.yrris.project.constant.CommonConstant;
import com.yrris.project.exception.BusinessException;
import com.yrris.project.exception.ThrowUtils;
import com.yrris.project.mapper.InterfaceInfoMapper;
import com.yrris.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.yrris.project.model.vo.InterfaceInfoVO;
import com.yrris.project.service.InterfaceInfoService;
import com.yrris.project.service.UserService;
import com.yrris.project.utils.SqlUtil;
import com.yrris.project.model.entity.InterfaceInfo;
import com.yrris.project.model.entity.User;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo> implements InterfaceInfoService {


    @Resource
    private UserService userService;
    private static final String[] METHODS = {"GET", "POST", "PUT", "DELETE", "HEAD", "TRACE", "OPTIONS", "CONNECT", "PATCH"};

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {

        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        String description = interfaceInfo.getDescription();
        //String requestParams = interfaceInfo.getRequestParams();
        //String requestHeader = interfaceInfo.getRequestHeader();
        //String responseHeader = interfaceInfo.getResponseHeader();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, url, method), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口名称过长");
        }
        if (StringUtils.isNotBlank(url) && url.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口地址过长");
        }
        if(StringUtils.isNotBlank(description)&& description.length()>256){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口描述过长");
        }
        for (String m : METHODS) {
            if (m.equals(method.toUpperCase())) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求类型不合法");
    }

    /**
     * 获取查询包装类
     *
     * @param interfaceInfoQueryRequest 接口查询请求
     * @return 查询包装
     */
    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (interfaceInfoQueryRequest == null) {
            return queryWrapper;
        }
        Long id = interfaceInfoQueryRequest.getId();
        String name = interfaceInfoQueryRequest.getName();
        String url = interfaceInfoQueryRequest.getUrl();
        Integer status = interfaceInfoQueryRequest.getStatus();
        String method = interfaceInfoQueryRequest.getMethod();
        String requestParams = interfaceInfoQueryRequest.getRequestParams();
        String requestHeader = interfaceInfoQueryRequest.getRequestHeader();
        String responseHeader = interfaceInfoQueryRequest.getResponseHeader();
        String description = interfaceInfoQueryRequest.getDescription();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        // 拼接查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.and(qw -> qw.like("name", name).or().like("description", name));
        }
        queryWrapper.like(StringUtils.isNotBlank(url), "url", url);
        queryWrapper.like(StringUtils.isNotBlank(method), "method", method);
        queryWrapper.like(StringUtils.isNotBlank(requestParams), "requestParams", requestParams);
        queryWrapper.like(StringUtils.isNotBlank(requestHeader), "requestHeader", requestHeader);
        queryWrapper.like(StringUtils.isNotBlank(responseHeader), "responseHeader", responseHeader);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.orderBy(SqlUtil.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request) {
        return InterfaceInfoVO.objToVo(interfaceInfo);
    }

    @Override
    public Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords();
        Page<InterfaceInfoVO> interfaceInfoVOPage = new Page<>(interfaceInfoPage.getCurrent(), interfaceInfoPage.getSize(), interfaceInfoPage.getTotal());
        if (CollUtil.isEmpty(interfaceInfoList)) {
            return interfaceInfoVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = interfaceInfoList.stream().map(InterfaceInfo::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> interfaceInfoIdSet = interfaceInfoList.stream().map(InterfaceInfo::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
        }
        // 填充信息
        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);
            Long userId = interfaceInfo.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        interfaceInfoVOPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVOPage;
    }

}
