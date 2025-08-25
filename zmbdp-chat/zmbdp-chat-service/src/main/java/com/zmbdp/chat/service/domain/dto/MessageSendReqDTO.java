package com.zmbdp.chat.service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 消息发送参数
 *
 * @author 稚名不带撇
 */
@Data
public class MessageSendReqDTO {

    @NotNull(message = "会话id不能为空")
    private Long sessionId;

    @NotNull(message = "消息发送方id不能为空")
    private Long fromId;

    /**
     * 不是为了存，是为了发送时少查一次表
     */
    @NotNull(message = "消息接收方id不能为空")
    private Long toId;

    /**
     * 如果是语音消息类型，将时间秒数放到正文中传参（json）
     */
    @Length(min = 1, max = 1024, message = "消息信息长度不能为空也不能超过1024")
    private String content;

    @NotNull(message = "消息类型不能为空")
    private Integer type;

    @NotBlank(message = "消息发送时间时间戳（毫秒）不能为空")
    private String createTime;

    // 服务端塞值

    private Long messageId;

    private Integer status;

    private Integer visited;
}