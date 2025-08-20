package com.zmbdp.admin.service.house.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zmbdp.common.core.domain.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * tag_house 标签房源对应关系表
 *
 * @author 稚名不带撇
 */
@Data
@TableName("tag_house")
@EqualsAndHashCode(callSuper=true)
public class TagHouse extends BaseDO {

    /**
     * 房源 id
     */
    private Long houseId;

    /**
     * 标签编码
     */
    private String tagCode;
}