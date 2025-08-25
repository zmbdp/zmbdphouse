package com.zmbdp.chat.service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 消息列表查询参数
 *
 * @author 稚名不带撇
 */
@Data
public class MessageListReqDTO implements Serializable {

    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @NotBlank(message = "最后一条消息ID不能为空")
    private String lastMessageId;

    /**
     * 获取消息数量
     */
    private Integer count = 10;

    /**
     * 是否获取当前消息id内容
     */
    private Boolean needCurMessage = false;
}