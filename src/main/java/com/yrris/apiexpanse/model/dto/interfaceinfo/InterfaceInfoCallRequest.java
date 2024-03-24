package com.yrris.apiexpanse.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口调用请求
 */
@Data
public class InterfaceInfoCallRequest implements Serializable {

    /**
     * 接口id
     */
    private Long id;

    /**
     * 用户的请求参数
     */
    private String userRequestParams;


    private static final long serialVersionUID = 1L;
}