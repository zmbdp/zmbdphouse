package com.zmbdp.portal.service.homepage.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 城市描述 VO
 *
 * @author 稚名不带撇
 */
@Data
public class CityDescVO implements Serializable {

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