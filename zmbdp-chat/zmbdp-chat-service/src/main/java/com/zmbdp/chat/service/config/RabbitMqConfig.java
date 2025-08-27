package com.zmbdp.chat.service.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {

    /**
     * 用户聊天消息的交换机名称
     */
    public static final String EXCHANGE_NAME = "chat_message_exchange";

    /**
     * 声明一个用户消息交换机
     */
    @Bean(EXCHANGE_NAME)
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(EXCHANGE_NAME, true, false);
    }
}