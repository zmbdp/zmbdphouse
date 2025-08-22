package com.zmbdp.admin.service.house.domain.dto;

import com.zmbdp.common.domain.domain.dto.BasePageReqDTO;
import lombok.Data;

/**
 * 房源列表查询参数 DTO
 *
 * @author 稚名不带撇
 */
@Data
public class HouseListReqDTO extends BasePageReqDTO {

    /**
     * 房源 id
     */
    private Long houseId;

    /**
     * 房源名称
     */
    private String title;

    /**
     * 房源类型
     */
    private String rentType;

    /**
     * 房源状态
     */
    private String status;

    /**
     * 所在城市
     */
    private Long cityId;

    /**
     * 所在小区
     */
    private String communityName;
}