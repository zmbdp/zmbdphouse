package com.zmbdp.admin.service.house.service.filter;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 租金范围筛选策略
 *
 * @author 稚名不带撇
 */
@Slf4j
@Component
public class RentalRangesFilter implements IHouseFilter {

    /**
     * 根据金额筛选实现方法
     *
     * @param houseDTO 房源
     * @param reqDTO 筛选条件
     * @return 筛选结果
     */
    @Override
    public Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO) {
        return CollectionUtils.isEmpty(reqDTO.getRentalRanges())
                || filterHouseByRentalRanges(houseDTO.getPrice(), reqDTO.getRentalRanges());
    }

    private boolean filterHouseByRentalRanges(Double price, List<String> rentalRanges) {
        if (null == price) {
            return false;
        }

        boolean isPriceInRange = false;
        for (String rentalRange : rentalRanges) {
            // 1800
            // [range_1, range_3]
            switch (rentalRange) {
                case "range_1":
                    isPriceInRange = price < 1000;
                    break;
                case "range_2":
                    isPriceInRange = price >= 1000 && price < 1500;
                    break;
                case "range_3":
                    isPriceInRange = price >= 1500 && price < 2000;
                    break;
                case "range_4":
                    isPriceInRange = price >= 2000 && price < 3000;
                    break;
                case "range_5":
                    isPriceInRange = price >= 3000 && price < 5000;
                    break;
                case "range_6":
                    isPriceInRange = price >= 5000;
                    break;
                default:
                    log.error("超出资金筛选范围, rentalRange:{}", rentalRange);
                    break;
            }
            if (isPriceInRange) {
                return true;
            }
        }
        return false;
    }

}
