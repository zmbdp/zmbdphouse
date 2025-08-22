package com.zmbdp.admin.service.house.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zmbdp.admin.service.house.domain.dto.HouseDescDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseListReqDTO;
import com.zmbdp.admin.service.house.domain.entity.House;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 房源表 Mapper
 *
 * @author 稚名不带撇
 */
@Mapper
public interface HouseMapper extends BaseMapper<House> {

    /**
     * 查询房源列表总数
     *
     * @param houseListReqDTO 查询参数
     * @return 房源列表
     */
    Long selectCountWithStatus(HouseListReqDTO houseListReqDTO);

    /**
     * 查询房源信息列表
     *
     * @param houseListReqDTO 搜索参数
     * @return 房源列表
     */
    List<HouseDescDTO> selectPageWithStatus(HouseListReqDTO houseListReqDTO);

    /**
     * 查询所有房源 id
     *
     * @return 房源 id列表
     */
    List<Long> selectHouseIds();
}
