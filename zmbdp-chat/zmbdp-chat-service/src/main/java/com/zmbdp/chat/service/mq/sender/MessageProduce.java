package com.zmbdp.chat.service.mq.sender;

import com.zmbdp.chat.service.config.RabbitMqConfig;
import com.zmbdp.chat.service.domain.dto.MessageSendReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 咨询消息发送
 *
 * @author 稚名不带撇
 */
@Slf4j
@Component
public class MessageProduce {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     *
     * @param messageSendReqDTO 消息实体
     */
    public void sendMessage(MessageSendReqDTO messageSendReqDTO) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, "", messageSendReqDTO);
        } catch (Exception e) {
            log.error("咨询聊天消息发送异常！", e);
        }
    }
}