package com.zmbdp.portal.service.homepage.domain.dto;

import lombok.Data;

/**
 * 城市描述 DTO
 *
 * @author 稚名不带撇
 */
@Data
public class CityDescDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 城市名称
     */
    private String name;

    /**
     * 城市全名
     */
    private String fullName;
}
