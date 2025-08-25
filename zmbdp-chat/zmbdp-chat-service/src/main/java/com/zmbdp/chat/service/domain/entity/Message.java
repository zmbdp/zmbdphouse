package com.zmbdp.chat.service.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 消息
 *
 * @author 稚名不带撇
 */
@Data
@TableName("message")
public class Message {

    /**
     * 消息id
     */
    private Long id;

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
     * 消息状态 0未读 1已读
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