package com.zmbdp.admin.service.house.service.strategy;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.service.house.domain.dto.HouseDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 距离排序策略
 *
 * @author 稚名不带撇
 */
public class DistanceSortStrategy implements ISortStrategy {

    private static final DistanceSortStrategy INSTANCE = new DistanceSortStrategy();

    private DistanceSortStrategy() {
    }

    public static DistanceSortStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public List<HouseDTO> sort(List<HouseDTO> houseDTOList, SearchHouseListReqDTO reqDTO) {
        return houseDTOList.stream()
                .sorted(Comparator.comparingDouble(
                        houseDTO -> houseDTO.calculateDistance(reqDTO.getLongitude(), reqDTO.getLatitude()))
                ).collect(Collectors.toList());
    }
}
