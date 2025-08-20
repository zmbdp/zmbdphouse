package com.zmbdp.admin.service.house.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房源状态枚举
 *
 * @author 稚名不带撇
 */
@Getter
@AllArgsConstructor
public enum HouseStatusEnum {

    UP("上架中"),
    DOWN("已下架"),
    RENTING("出租中"),
    ;

    /**
     * 描述
     */
    private String desc;

    /**
     * 根据名称获取枚举
     *
     * @param name 名称
     * @return 枚举
     */
    public static HouseStatusEnum getByName(String name) {
        for (HouseStatusEnum houseStatusEnum : HouseStatusEnum.values()) {
            if (houseStatusEnum.name().equalsIgnoreCase(name)) {
                return houseStatusEnum;
            }
        }
        return null;
    }
}
