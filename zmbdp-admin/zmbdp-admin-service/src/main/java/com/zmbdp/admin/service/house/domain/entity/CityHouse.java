package com.zmbdp.admin.service.house.domain.entity;

import com.zmbdp.common.core.domain.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * city_house 城市房源对应表
 *
 * @author 稚名不带撇
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class CityHouse extends BaseDO {

    /**
     * 城市 id
     */
    private Long cityId;

    /**
     * 城市名
     */
    private String cityName;

    /**
     * 房源 id
     */
    private Long houseId;
}