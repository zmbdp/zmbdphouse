package com.zmbdp.admin.service.house.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 房源状态修改请求参数 DTO
 *
 * @author 稚名不带撇
 */
@Data
public class HouseStatusEditReqDTO implements Serializable {

    /**
     * 房源 id
     */
    @NotNull(message = "房源Id不能为空！")
    private Long houseId;

    /**
     * 要修改的类型
     */
    @NotBlank(message = "要修改的类型不能为空！")
    private String status;

    /**
     * 出租时长
     */
    private String rentTimeCode;
}