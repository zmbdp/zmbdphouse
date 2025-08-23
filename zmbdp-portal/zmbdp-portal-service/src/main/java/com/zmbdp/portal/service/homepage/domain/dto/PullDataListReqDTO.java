package com.zmbdp.portal.service.homepage.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 拉取区域数据列表 DTO
 *
 * @author 稚名不带撇
 */
@Data
public class PullDataListReqDTO implements Serializable {

    /**
     * 城市id
     */
    @NotNull(message = "城市id不能为空！")
    private Long cityId;

    /**
     * 字典类型
     */
    @NotEmpty(message = "字典类型不能为空！")
    private List<String> dirtTypes;
}