package com.yrris.apiexpanse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yrris.apiexpanse.annotation.AuthCheck;
import com.yrris.apiexpanse.common.*;
import com.yrris.apiexpanse.constant.UserConstant;
import com.yrris.apiexpanse.exception.BusinessException;
import com.yrris.apiexpanse.exception.ThrowUtils;
import com.yrris.apiexpanse.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.yrris.apiexpanse.model.dto.interfaceinfo.InterfaceInfoCallRequest;
import com.yrris.apiexpanse.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.yrris.apiexpanse.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.yrris.apiexpanse.model.entity.InterfaceInfo;
import com.yrris.apiexpanse.model.entity.User;
import com.yrris.apiexpanse.model.enums.InterfaceInfoStatusEnum;
import com.yrris.apiexpanse.model.vo.InterfaceInfoVO;
import com.yrris.apiexpanse.service.InterfaceInfoService;
import com.yrris.apiexpanse.service.UserService;
import com.yrris.apiexpansesdk.client.ApiExpanseClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 接口信息的接口
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    //引入客户端实例
    @Resource
    private ApiExpanseClient apiExpanseClient;


    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest 创建接口请求
     * @param request                 请求信息
     * @return 接口id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest 删除接口请求
     * @param request       请求信息
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param interfaceInfoUpdateRequest 更新接口请求
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 发布接口（仅管理员）
     *
     * @param idRequest 接口id封装
     * @return 是否成功
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            // 请求参数错误
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.检验接口是否存在
        Long id = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            //请求接口数据不存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //2.检查接口是否为可调用状态
        // fixme 此时简单模拟测试接口连接状态 需要启动示例接口项目
        com.yrris.apiexpansesdk.model.User user = new com.yrris.apiexpansesdk.model.User();
        user.setUserName("root");
        String userNameByPost = apiExpanseClient.getUserNameByPost(user);
        if (StringUtils.isBlank(userNameByPost)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
        }
        //3.修改接口信息为开启状态（status = 1）
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 下线接口（仅管理员）
     *
     * @param idRequest 接口id封装
     * @return 是否成功
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            // 请求参数错误
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.检验接口是否存在
        Long id = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            //请求接口数据不存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //2.修改接口信息为关闭状态（status = 0）
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 用户调用接口
     *
     * @param apiCallRequest 接口调用请求
     * @return 是否成功
     */
    @PostMapping("/call")
    public BaseResponse<Object> callInterfaceInfo(@RequestBody InterfaceInfoCallRequest apiCallRequest,HttpServletRequest request) {
        if (apiCallRequest == null || apiCallRequest.getId()<=0) {
            // 请求参数错误
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.检验接口是否存在
        Long id = apiCallRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            //请求接口数据不存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //2.检查接口是否为下线状态
        if(interfaceInfo.getStatus() == InterfaceInfoStatusEnum.OFFLINE.getValue()){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"当前接口下线，请稍后重试！");
        }
        //获取用户请求参数
        String userRequestParams = apiCallRequest.getUserRequestParams();
        //获取当前登录用户和签名
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        //3.使用当前登录用户的认证签名进行接口调用
        ApiExpanseClient apiClient = new ApiExpanseClient(accessKey, secretKey);
        Gson gson =new Gson();
        com.yrris.apiexpansesdk.model.User user = gson.fromJson(userRequestParams, com.yrris.apiexpansesdk.model.User.class);
        return ResultUtils.success(apiClient.getUserNameByPost(user));
    }


    /**
     * 根据 id 获取
     *
     * @param id 接口id
     * @return 接口视图
     */
    @GetMapping("/get/vo")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVO(interfaceInfo, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param interfaceInfoQueryRequest 查询接口请求
     * @return 接口分页
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest 查询接口请求
     * @param request                   请求信息
     * @return 接口视图分页
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                         HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage, request));
    }

    // endregion

}
