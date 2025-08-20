package com.zmbdp.admin.service.house.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zmbdp.common.core.domain.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * house_status 房源状态表
 *
 * @author 稚名不带撇
 */
@Data
@TableName("house_status")
@EqualsAndHashCode(callSuper=true)
public class HouseStatus extends BaseDO {
    /**
     * 房源 id
     */
    private Long houseId;

    /**
     * 房源状态
     */
    private String status;

    // 出租时间码
    /**
     * 出租时长（字典配置）
     */
    private String rentTimeCode;

    // 存毫秒级时间戳
    /**
     * 出租开始时间
     */
    private Long rentStartTime;

    /**
     * 出租结束时间
     */
    private Long rentEndTime;
}