package com.zmbdp.admin.service.house.service.filter;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 房间数过滤器
 *
 * @author 稚名不带撇
 */
@Component
public class RoomsFilter implements IHouseFilter {
    @Override
    public Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO) {
        return CollectionUtils.isEmpty(reqDTO.getRooms())
                || reqDTO.getRooms().contains(houseDTO.getRooms());
    }
}
