package com.zmbdp.portal.service.house.domain.vo;

import com.zmbdp.admin.api.house.domain.dto.DeviceDTO;
import com.zmbdp.admin.api.house.domain.dto.TagDTO;
import lombok.Data;

import java.util.List;

/**
 * 前端房源详情页展示的房源数据
 *
 * @author 稚名不带撇
 */
@Data
public class HouseDataVO {
    private Long houseId;
    private Long userId;
    private String nickName;
    private String title;
    private String rentType;
    private Integer floor;
    private Integer allFloor;
    private String houseType;
    private String rooms;
    private String position;
    private Double area;
    private Double price;
    private String intro;
    private List<DeviceDTO> devices;
    private List<TagDTO> tags;
    private String headImage;
    private List<String> images;
    private Long cityId;
    private String cityName;
    private Long regionId;
    private String regionName;
    private String communityName;
    private String detailAddress;
    private Double longitude;
    private Double latitude;
}