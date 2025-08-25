package com.zmbdp.chat.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息体
 *
 * @author 稚名不带撇
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketDTO<T> {

    private String type;

    private T data;

}