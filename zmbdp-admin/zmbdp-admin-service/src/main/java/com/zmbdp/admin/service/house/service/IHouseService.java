package com.zmbdp.admin.service.house.service;

import com.zmbdp.admin.service.house.domain.dto.HouseAddOrEditReqDTO;

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
}
