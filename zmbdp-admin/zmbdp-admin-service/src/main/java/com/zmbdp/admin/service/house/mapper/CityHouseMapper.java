package com.zmbdp.admin.service.house.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zmbdp.admin.service.house.domain.entity.CityHouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 城市房源对应表 Mapper
 *
 * @author 稚名不带撇
 */
@Mapper
public interface CityHouseMapper extends BaseMapper<CityHouse> {
}
