package com.zmbdp.admin.service.house.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 房源详细信息 VO
 *
 * @author 稚名不带撇
 */
@Data
public class HouseVO implements Serializable {
    /**
     * 房源 id
     */
    private Long houseId;

    /**
     * 房东 id
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 出租类型
     */
    private String rentType;

    /**
     * 价格（元）
     */
    private Double price;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 社区名称
     */
    private String communityName;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 房源状态
     */
    private String status;

    /**
     * 出租时间段
     */
    private String rentTimeCode;
}