package com.zmbdp.admin.service.house.service;

import com.zmbdp.admin.service.house.domain.dto.HouseAddOrEditReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;

/**
 * 房源服务业务层接口
 *
 * @author 稚名不带撇
 */
public interface IHouseService {

    /**
     * 添加或编辑房源
     *
     * @param houseAddOrEditReqDTO 房源信息
     * @return 房源 ID
     */
    Long addOrEdit(HouseAddOrEditReqDTO houseAddOrEditReqDTO);

    /**
     * 更新房源缓存
     *
     * @param houseId 房源 ID
     */
    void cacheHouse(Long houseId);

    /**
     * 查询房源详情（带缓存）
     *
     * @param houseId 房源 id
     * @return 房源详情 DTO
     */
    HouseDTO detail(Long houseId);
}
