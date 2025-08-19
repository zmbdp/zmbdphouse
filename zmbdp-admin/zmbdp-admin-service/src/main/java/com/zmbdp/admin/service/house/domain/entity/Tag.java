package com.zmbdp.admin.service.house.domain.entity;

import com.zmbdp.common.core.domain.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * tag 标签枚举表
 *
 * @author 稚名不带撇
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class Tag extends BaseDO {

    /**
     * 标签编码
     */
    private String tagCode;

    /**
     * 标签名称
     */
    private String tagName;
}