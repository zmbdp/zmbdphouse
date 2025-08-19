package com.zmbdp.admin.service.house.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zmbdp.admin.service.house.domain.entity.HouseStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房源状态表 Mapper
 *
 * @author 稚名不带撇
 */
@Mapper
public interface HouseStatusMapper extends BaseMapper<HouseStatus> {
}
