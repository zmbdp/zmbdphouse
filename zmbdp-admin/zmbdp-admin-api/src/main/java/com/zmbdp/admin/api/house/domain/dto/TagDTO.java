package com.zmbdp.admin.api.house.domain.dto;

import lombok.Data;

/**
 * 标签 DTO
 *
 * @author 稚名不带撇
 */
@Data
public class TagDTO {

    /**
     * 标签码
     */
    private String tagCode;

    /**
     * 标签名
     */
    private String tagName;
}