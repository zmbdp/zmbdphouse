package com.zmbdp.admin.service.house.service.filter;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;
import org.springframework.stereotype.Component;

/**
 * 区筛选策略
 *
 * @author 稚名不带撇
 */
@Component
public class RegionFilter implements IHouseFilter {

    /**
     * 区筛选策略
     *
     * @param houseDTO 房源
     * @param reqDTO   区域筛选条件
     * @return 是否通过筛选
     */
    @Override
    public Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO) {
        // 不设置区域筛选条件
        // 传递的区域筛选条件与房源的所在区一致
        return null == reqDTO.getRegionId() || houseDTO.getRegionId().equals(reqDTO.getRegionId());
    }
}
