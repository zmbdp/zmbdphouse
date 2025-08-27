package com.zmbdp.chat.service.mq.listener;

import com.zmbdp.chat.service.config.RabbitMqConfig;
import com.zmbdp.chat.service.domain.dto.MessageSendReqDTO;
import com.zmbdp.chat.service.domain.dto.WebSocketDTO;
import com.zmbdp.chat.service.domain.enums.WebSocketDataTypeEnum;
import com.zmbdp.chat.service.service.websocket.WebSocketServer;
import com.zmbdp.common.core.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

/**
 * 推送消息给目标用户
 *
 * @author 稚名不带撇
 */
@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(),
        exchange = @Exchange(value = RabbitMqConfig.EXCHANGE_NAME, type = ExchangeTypes.FANOUT)
)})
public class MessageForwardConsume {

    @RabbitHandler
    public void process(MessageSendReqDTO messageSendReqDTO) {
        try {
            WebSocketDTO<String> webSocketDTO = new WebSocketDTO<>();
            webSocketDTO.setType(WebSocketDataTypeEnum.CHAT.getType());
            webSocketDTO.setData(JsonUtil.classToJson(messageSendReqDTO));
            // 支持消息丢弃：判断当前服务器是否维护了目标用户连接
            WebSocketServer.sendMessage(messageSendReqDTO.getToId(), webSocketDTO);
        } catch (Exception e) {
            log.error("聊天消息转发失败:{}", JsonUtil.classToJson(messageSendReqDTO), e);
        }
    }
}