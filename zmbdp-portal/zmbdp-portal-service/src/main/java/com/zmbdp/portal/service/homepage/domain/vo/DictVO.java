package com.zmbdp.portal.service.homepage.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据 VO
 *
 * @author 稚名不带撇
 */
@Data
public class DictVO implements Serializable {

    private Long id;

    private String key;

    private String name;
}