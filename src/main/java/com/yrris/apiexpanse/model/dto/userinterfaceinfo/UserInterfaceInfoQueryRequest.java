package com.yrris.apiexpanse.model.dto.userinterfaceinfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.yrris.apiexpanse.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserInterfaceInfoQueryRequest extends PageRequest implements Serializable {

    /**
     * 通过主键查询使用次数较少，保留
     */
    private Long id;

    /**
     * 根据调用用户id查询
     */
    private Long userId;

    /**
     * 根据接口id查询使用信息
     */
    private Long interfaceInfoId;

    /**
     * 总调用次数（可用于范围查询）
     */
    private Integer totalNum;

    /**
     * 剩余调用次数（可用于范围查询）
     */
    private Integer leftNum;

    /**
     * 根据状态查询 0-正常，1-禁用
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}