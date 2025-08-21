package com.zmbdp.admin.service.house.domain.dto;

import com.zmbdp.admin.api.house.domain.dto.DeviceDTO;
import com.zmbdp.admin.api.house.domain.dto.TagDTO;
import com.zmbdp.admin.api.house.domain.vo.HouseDetailVO;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import lombok.Data;

import java.util.List;

/**
 * 完整的房源信息
 *
 * @author 稚名不带撇
 */
@Data
public class HouseDTO {

    private Long houseId;
    // 房东信息
    private Long userId;
    private String nickName;
    private String avatar;
    // 房屋基本信息
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

    // 设备列表
    private List<DeviceDTO> devices;
    // 标签列表
    private List<TagDTO> tags;

    private String headImage;
    private List<String> images;

    // 位置信息
    private Long cityId;
    private String cityName;
    private Long regionId;
    private String regionName;
    private String communityName;
    private String detailAddress;
    private Double longitude;
    private Double latitude;

    // 状态信息
    private String status;
    private String rentTimeCode;


    /**
     * 计算两地经纬度距离
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 距离
     */
    public double calculateDistance(Double longitude, Double latitude) {
        return Math.sqrt(Math.pow(this.longitude - longitude, 2) +
                Math.pow(this.latitude - latitude, 2));
    }

    /**
     * 将 HouseDTO 转换为 HouseDetailVO
     *
     * @return HouseDetailVO
     */
    public HouseDetailVO convertToVO() {
        HouseDetailVO houseDetailVO = new HouseDetailVO();
        BeanCopyUtil.copyProperties(this, houseDetailVO);
        return houseDetailVO;
    }

}
