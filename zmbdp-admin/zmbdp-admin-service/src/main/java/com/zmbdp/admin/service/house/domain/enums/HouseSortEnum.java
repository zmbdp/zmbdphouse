package com.zmbdp.admin.service.house.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房源排序枚举
 *
 * @author 稚名不带撇
 */
@Getter
@AllArgsConstructor
public enum HouseSortEnum {

    DISTANCE("距离优先"),
    PRICE_DESC("价格从高到低"),
    PRICE_ASC("价格从低到高"),
    ;
    private String desc;
}
