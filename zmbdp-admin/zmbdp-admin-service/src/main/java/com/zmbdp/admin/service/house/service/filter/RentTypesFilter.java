package com.zmbdp.admin.service.house.service.filter;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 出租类型策略
 *
 * @author 稚名不带撇
 */
@Component
public class RentTypesFilter implements IHouseFilter {
    @Override
    public Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO) {
        return CollectionUtils.isEmpty(reqDTO.getRentTypes())
                || reqDTO.getRentTypes().contains(houseDTO.getRentType());
    }
}
