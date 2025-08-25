package com.zmbdp.chat.service.domain.dto;

import lombok.Data;

/**
 * 消息 DTO
 *
 * @author 稚名不带撇
 */
@Data
public class MessageDTO {
    /**
     * 消息 id
     */
    private String messageId;

    /**
     * 发送方id
     */
    private Long fromId;

    /**
     * 会话id
     */
    private Long sessionId;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 消息正文
     */
    private String content;

    /**
     * 语音消息状态 0未读 1已读
     */
    private Integer status;

    /**
     * 消息是否浏览 10未浏览 11浏览过
     */
    private Integer visited;

    /**
     * 消息发送时间(时间戳：毫秒级)
     */
    private Long createTime;
}