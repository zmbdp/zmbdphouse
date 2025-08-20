package com.zmbdp.admin.service.house.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zmbdp.common.core.domain.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * city_house 城市房源对应表
 *
 * @author 稚名不带撇
 */
@Data
@TableName("city_house")
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