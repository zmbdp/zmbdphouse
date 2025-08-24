package com.zmbdp.admin.service.house.service.strategy;

import com.zmbdp.admin.service.house.domain.enums.HouseSortEnum;
import com.zmbdp.common.core.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 排序策略工厂类
 *
 * @author 稚名不带撇
 */
@Slf4j
public class SortStrategyFactory {

    /**
     * 根据排序规则返回对应的排序策略
     */
    public static ISortStrategy getSortStrategy(String sort) {

        if (StringUtil.isNotEmpty(sort)) {
            if (sort.equalsIgnoreCase(HouseSortEnum.DISTANCE.name())) {
                return DistanceSortStrategy.getInstance();
            } else if (sort.equalsIgnoreCase(HouseSortEnum.PRICE_ASC.name())) {
                return PriceSortStrategy.getInstance(true);
            } else if (sort.equalsIgnoreCase(HouseSortEnum.PRICE_DESC.name())) {
                return PriceSortStrategy.getInstance(false);
            } else {
                log.error("不存在的排序规则，将按照距离排序！");
                return DistanceSortStrategy.getInstance();
            }
        }
        return DistanceSortStrategy.getInstance();
    }
}
