package com.zmbdp.admin.service.house.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zmbdp.admin.service.house.domain.entity.TagHouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房源标签对应表 Mapper
 *
 * @author 稚名不带撇
 */
@Mapper
public interface TagHouseMapper extends BaseMapper<TagHouse> {
}
