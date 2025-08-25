package com.zmbdp.chat.service.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消息浏览参数
 *
 * @author 稚名不带撇
 */
@Data
public class MessageVisitedReqDTO {
    /**
     * 会话 id
     */
    @NotNull(message = "会话id不能为空！")
    private Long sessionId;
}