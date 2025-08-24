package com.zmbdp.admin.service.house.service.strategy;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 根据价格排序
 *
 * @author 稚名不带撇
 */
public class PriceSortStrategy implements ISortStrategy {

    private final boolean asc;
    private final static PriceSortStrategy ASC_INSTANCE = new PriceSortStrategy(true);
    private final static PriceSortStrategy DESC_INSTANCE = new PriceSortStrategy(false);

    private PriceSortStrategy(boolean asc) {
        this.asc = asc;
    }

    public static PriceSortStrategy getInstance(boolean asc) {
        return asc ? ASC_INSTANCE : DESC_INSTANCE;
    }

    @Override
    public List<HouseDTO> sort(List<HouseDTO> houseDTOList, SearchHouseListReqDTO reqDTO) {
        if (asc) {
            return houseDTOList.stream()
                    .sorted(Comparator.comparingDouble(HouseDTO::getPrice))
                    .collect(Collectors.toList());
        } else {
            return houseDTOList.stream()
                    .sorted(Comparator.comparingDouble(HouseDTO::getPrice).reversed())
                    .collect(Collectors.toList());
        }
    }
}
