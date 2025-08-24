package com.zmbdp.admin.service.house.service.filter;


import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;

/**
 * 房源过滤器
 *
 * @author 稚名不带撇
 */
public interface IHouseFilter {

    /**
     * 过滤房源
     *
     * @param houseDTO
     * @param reqDTO
     * @return
     */
    Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO);
}
