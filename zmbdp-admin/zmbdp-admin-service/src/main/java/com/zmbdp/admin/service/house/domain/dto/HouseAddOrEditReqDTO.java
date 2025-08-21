package com.zmbdp.admin.service.house.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 房源添加或编辑 DTO
 *
 * @author 稚名不带撇
 */
@Data
public class HouseAddOrEditReqDTO implements Serializable {

    /**
     * 房源 id
     */
    private Long houseId;

    /**
     * 房东 id
     */
    @NotNull(message = "房东id不能为空！")
    private Long userId;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空！")
    private String title;

    /**
     * 出租类型
     */
    @NotBlank(message = "出租类型不能为空！")
    private String rentType;

    /**
     * 所在楼层
     */
    @NotNull(message = "所在楼层不能为空！")
    private Integer floor;

    /**
     * 总楼层
     */
    @NotNull(message = "总楼层不能为空！")
    private Integer allFloor;

    /**
     * 户型
     */
    @NotBlank(message = "户型不能为空！")
    private String houseType;

    /**
     * 居室
     */
    @NotBlank(message = "居室不能为空！")
    private String rooms;

    /**
     * 朝向
     */
    @NotBlank(message = "朝向不能为空！")
    private String position;

    /**
     * 面积（平方米）
     */
    @NotNull(message = "面积（平方米）不能为空！")
    private Double area;

    /**
     * 价格（元）
     */
    @NotNull(message = "价格（元）不能为空！")
    private Double price;

    /**
     * 介绍
     */
    @NotBlank(message = "介绍不能为空！")
    private String intro;

    /**
     * 设备列表
     */
    @NotEmpty(message = "设备列表不能为空！")
    private List<String> devices;

    /**
     * 标签列表
     */
    @NotEmpty(message = "标签列表不能为空！")
    private List<String> tagCodes;

    /**
     * 头图
     */
    @NotBlank(message = "头图不能为空！")
    private String headImage;

    /**
     * 图片列表
     */
    private List<String> images;

    /**
     * 城市 id
     */
    @NotNull(message = "城市id不能为空！")
    private Long cityId;

    /**
     * 城市名
     */
    @NotBlank(message = "城市名不能为空！")
    private String cityName;

    /**
     * 区域名
     */
    @NotNull(message = "区域id不能为空！")
    private Long regionId;

    /**
     * 区域名
     */
    @NotBlank(message = "区域名不能为空！")
    private String regionName;

    /**
     * 社区名
     */
    @NotBlank(message = "社区名不能为空！")
    private String communityName;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空！")
    private String detailAddress;

    /**
     * 房源经度
     */
    @NotNull(message = "房源经度不能为空！")
    private Double longitude;

    /**
     * 房源纬度
     */
    @NotNull(message = "房源纬度不能为空！")
    private Double latitude;
}