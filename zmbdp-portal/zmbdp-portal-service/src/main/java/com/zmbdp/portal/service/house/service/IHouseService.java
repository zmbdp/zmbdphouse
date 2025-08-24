package com.zmbdp.portal.service.house.service;

import com.zmbdp.portal.service.house.domain.vo.HouseDataVO;

/**
 * 房源服务接口
 *
 * @author 稚名不带撇
 */
public interface IHouseService {

    /**
     * C端 根据房源 id 查询房源详情
     *
     * @param houseId 房源 id
     * @return 房源详情
     */
    HouseDataVO houseDetail(Long houseId);
}
