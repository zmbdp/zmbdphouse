package com.zmbdp.chat.service.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 消息阅读参数
 *
 * @author 稚名不带撇
 */
@Data
public class MessageReadReqDTO {
    /**
     * 会话 id
     */
    @NotNull(message = "会话id不能为空！")
    private Long sessionId;

    /**
     * 消息 id 列表
     */
    @NotEmpty(message = "消息id列表不能为空！")
    private List<String> messageIds;
}
