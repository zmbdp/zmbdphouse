package com.zmbdp.admin.service.house.service.filter;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;
import com.zmbdp.admin.service.house.domain.enums.HouseStatusEnum;
import org.springframework.stereotype.Component;

/**
 * 状态过滤器
 *
 * @author 稚名不带撇
 */
@Component
public class StatusFilter implements IHouseFilter {
    @Override
    public Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO) {
        return houseDTO.getStatus().equalsIgnoreCase(HouseStatusEnum.UP.name());
    }
}
