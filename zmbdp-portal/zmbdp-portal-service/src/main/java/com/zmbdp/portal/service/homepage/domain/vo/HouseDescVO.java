package com.zmbdp.portal.service.homepage.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户筛选条件得出的房源 VO
 *
 * @author 稚名不带撇
 */
@Data
public class HouseDescVO implements Serializable {

    private Long houseId;

    private String headImage;

    private String title;

    private String rentType;

    private Double area;

    private String position;

    private String regionName;

    private Double price;
}