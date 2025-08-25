package com.zmbdp.chat.service.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息VO
 *
 * @author 稚名不带撇
 */
@Data
public class MessageVO implements Serializable {

    /**
     * 消息id
     */
    private String messageId;

    /**
     * 会话id
     */
    private Long sessionId;

    /**
     * 发送者id
     */
    private Long fromId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 消息状态
     */
    private Integer status;

    /**
     * 消息访问状态
     */
    private Integer visited;

    /**
     * 创建时间
     */
    private Long createTime;
}