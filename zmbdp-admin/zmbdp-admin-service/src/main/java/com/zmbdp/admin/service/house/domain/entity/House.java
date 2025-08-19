package com.zmbdp.admin.service.house.domain.entity;

import com.zmbdp.common.core.domain.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * house 房源表
 *
 * @author 稚名不带撇
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class House extends BaseDO {
    /**
     * 房东 id
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 租房类型
     */
    private String rentType;

    /**
     * 所在楼层
     */
    private Integer floor;

    /**
     * 总楼层
     */
    private Integer allFloor;

    /**
     * 户型
     */
    private String houseType;

    /**
     * 居室
     */
    private String rooms;

    /**
     * 朝向
     */
    private String position;

    /**
     * 面积（平方米）
     */
    private BigDecimal area;

    /**
     * 价格（元）
     */
    private BigDecimal price;

    /**
     * 房屋介绍
     */
    private String intro;

    /**
     * 设备列表
     */
    private String devices;

    /**
     * 头图
     */
    private String headImage;

    /**
     * 房源图
     */
    private String images;

    /**
     * 城市 id
     */
    private Long cityId;

    /**
     * 城市名
     */
    private String cityName;

    /**
     * 区域 id
     */
    private Long regionId;

    /**
     * 区域名
     */
    private String regionName;

    /**
     * 社区名（小区名）
     */
    private String communityName;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;
}