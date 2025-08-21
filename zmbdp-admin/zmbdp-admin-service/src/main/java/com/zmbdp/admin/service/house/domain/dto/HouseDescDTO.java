package com.zmbdp.admin.service.house.domain.dto;

import lombok.Data;

/**
 * 完整的房源信息 DTO
 *
 * @author 稚名不带撇
 */
@Data
public class HouseDescDTO {

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
    private String status;
    private String rentTimeCode;
}
