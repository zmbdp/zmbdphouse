package com.zmbdp.chat.service.mq.listener;


import com.zmbdp.chat.service.config.RabbitMqConfig;
import com.zmbdp.chat.service.domain.dto.MessageSendReqDTO;
import com.zmbdp.common.core.utils.JsonUtil;
import com.zmbdp.common.domain.exception.ServiceException;
import com.zmbdp.common.redis.service.RedissonLockService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 持久化聊天消息
 *
 * @author 稚名不带撇
 */
@Slf4j
@Component
@RabbitListener(bindings = {@QueueBinding(
        value = @Queue(),
        exchange = @Exchange(value = RabbitMqConfig.EXCHANGE_NAME, type = ExchangeTypes.FANOUT)
)})
public class MessageStoreConsume {

    /**
     * 存储聊天消息分布式锁 key
     */
    private static final String LOCK_KEY = "chat:db:lock";

    /**
     * 分布式锁服务
     */
    @Autowired
    private RedissonLockService redissonLockService;

    /**
     * 消息服务
     */
    @Autowired
    private IMessageService messageService;

    @RabbitHandler
    public void process(MessageSendReqDTO messageSendReqDTO) {
        // 获取分布式锁
        RLock lock =  redissonLockService.acquire(LOCK_KEY, -1);
        if (null == lock) {
            // 获取锁失败，跳过执行
            return;
        }

        try {
            // 幂等性处理：消息已存在，不处理
            if (null != messageService.get(messageSendReqDTO.getMessageId())) {
                return;
            }

            // 不存在，持久化存储
            if (!messageService.add(messageSendReqDTO)) {
                throw new ServiceException("聊天消息持久化失败！");
            }
        } catch (Exception e) {
            log.error("消息持久化异常！messageSendReqDTO:{}", JsonUtil.classToJson(messageSendReqDTO), e);
        } finally {
            // 释放锁
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                redissonLockService.releaseLock(lock);
            }
        }
    }
}