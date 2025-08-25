package com.zmbdp.chat.service.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zmbdp.common.core.domain.entity.BaseDO;
import lombok.Data;

/**
 * 会话
 *
 * @author 稚名不带撇
 */
@Data
@TableName("session")
public class Session extends BaseDO {
    private Long userId1;
    private Long userId2;
}
