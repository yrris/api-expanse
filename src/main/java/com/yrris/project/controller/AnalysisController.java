package com.yrris.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yrris.project.annotation.AuthCheck;
import com.yrris.project.common.BaseResponse;
import com.yrris.project.common.ErrorCode;
import com.yrris.project.common.ResultUtils;
import com.yrris.project.exception.BusinessException;
import com.yrris.project.mapper.UserInterfaceInfoMapper;
import com.yrris.project.model.entity.InterfaceInfo;
import com.yrris.project.model.entity.UserInterfaceInfo;
import com.yrris.project.model.vo.InterfaceInfoVO;
import com.yrris.project.service.InterfaceInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口调用分析
 */
@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    // fixme 应该在Service中
    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    private static final int DEFAULT_LIMIT = 3;

    @GetMapping("/top/interfaceInfo")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfoVO>> listTopNumInterfaceInfo(Integer num){
        //限制查询调用总次数最多的接口数量 默认为
        int limit = num==null ? DEFAULT_LIMIT : num;
        // 查询调用次数最多的接口信息列表
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopNumInterfaceInfo(limit);
        // 将接口信息按照接口ID分组，便于关联查询
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdObjMap = userInterfaceInfoList.stream()
                .collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        // 创建查询接口信息的条件包装器
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        // 设置查询条件，使用接口信息ID在接口信息映射中的键集合进行条件匹配
        queryWrapper.in("id", interfaceInfoIdObjMap.keySet());
        // 调用接口信息服务的list方法，传入条件包装器，获取符合条件的接口信息列表
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);
        // 判断查询结果是否为空
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 构建接口信息VO列表，使用流式处理将接口信息映射为接口信息VO对象，并加入列表中
        List<InterfaceInfoVO> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
            // 创建一个新的接口信息VO对象
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            // 将接口信息复制到接口信息VO对象中
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            // 从接口信息ID对应的映射中获取调用次数
            int totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalNum();
            // 将调用次数设置到接口信息VO对象中
            interfaceInfoVO.setTotalNum(totalNum);
            // 返回构建好的接口信息VO对象
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        // 返回处理结果
        return ResultUtils.success(interfaceInfoVOList);
    }
}
