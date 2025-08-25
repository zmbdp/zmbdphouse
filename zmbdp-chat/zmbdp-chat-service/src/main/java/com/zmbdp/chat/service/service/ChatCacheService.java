package com.zmbdp.chat.service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zmbdp.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 聊天缓存服务
 *
 * @author 稚名不带撇
 */
@Slf4j
@Component
public class ChatCacheService {

    /**
     * 用户 id - 会话 ids
     */
    private static final String CHAT_ZSET_USER_PREFIX = "chat:zset:user:";

    /**
     * 会话 id - 会话详细信息 DTO
     */
    private static final String CHAT_SESSION_PREFIX = "chat:session:";

    /**
     * 会话 id - 聊天消息列表（zset）
     */
    private static final String CHAT_ZSET_SESSION_PREFIX = "chat:zset:session:";

    /**
     * 聊天缓存服务
     */
    @Autowired
    private RedisService redisService;

    /**
     * 新增用户下的一个新会话
     *
     * @param userId          用户 id
     * @param sessionId       会话 id
     * @param lastSessionTime 排序规则：最后的会话时间
     */
    public void addUserSessionToCache(Long userId, Long sessionId, Long lastSessionTime) {
        try {
            redisService.addMemberZSet(CHAT_ZSET_USER_PREFIX + userId, sessionId, lastSessionTime);
        } catch (Exception e) {
            log.error("新增用户下的新会话 id 缓存时发生异常，userId:{}", userId, e);
        }
    }

    /**
     * 获取用户下的会话列表
     *
     * @param userId 用户 id
     * @return 该用户的会话 id 列表
     */
    public Set<Long> getUserSessionsByCache(Long userId) {
        Set<Long> sessionIds = new HashSet<>();
        try {
            sessionIds = redisService.getCacheZSetDesc(CHAT_ZSET_USER_PREFIX + userId, new TypeReference<LinkedHashSet<Long>>() {
            });
            if (CollectionUtils.isEmpty(sessionIds)) {
                return new HashSet<>();
            }
        } catch (Exception e) {
            log.error("从缓存中获取用户下的会话列表异常，userId:{}", userId, e);
        }
        return sessionIds;
    }
}
