package com.yrris.apiexpanse.model.dto.userinterfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 开通接口调用
 */
@Data
public class UserInterfaceInfoAddRequest implements Serializable {
    //用户id
    private Long userId;

    //接口id
    private Long interfaceInfoId;

    //总调用次数
    private Integer totalNum;

    //剩余调用次数
    private Integer leftNum;

    private static final long serialVersionUID = 1L;
}