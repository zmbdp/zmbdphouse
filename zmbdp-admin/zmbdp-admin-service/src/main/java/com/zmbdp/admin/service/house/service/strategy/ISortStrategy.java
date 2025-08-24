package com.zmbdp.admin.service.house.service.strategy;


import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;

import java.util.List;

/**
 * 排序策略
 *
 * @author 稚名不带撇
 */
public interface ISortStrategy {

    /**
     * 排序
     */
    List<HouseDTO> sort(List<HouseDTO> houseDTOList, SearchHouseListReqDTO reqDTO);
}
