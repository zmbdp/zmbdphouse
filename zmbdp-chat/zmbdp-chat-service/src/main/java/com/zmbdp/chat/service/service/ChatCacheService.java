package com.zmbdp.chat.service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zmbdp.chat.service.domain.dto.MessageDTO;
import com.zmbdp.chat.service.domain.dto.SessionStatusDetailDTO;
import com.zmbdp.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
@Service
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

    /**
     * 缓存会话详细信息
     *
     * @param sessionId  会话 id
     * @param sessionDTO 会话详细信息 DTO
     */
    public void cacheSessionDTO(Long sessionId, SessionStatusDetailDTO sessionDTO) {
        try {
            redisService.setCacheObject(CHAT_SESSION_PREFIX + sessionId, sessionDTO);
        } catch (Exception e) {
            log.error("缓存会话详细信息异常，sessionId:{}", sessionId, e);
        }
    }

    /**
     * 获取会话详细信息缓存
     *
     * @param sessionId 会话 id
     * @return 会话详细信息 DTO
     */
    public SessionStatusDetailDTO getSessionDTOByCache(Long sessionId) {
        try {
            return redisService.getCacheObject(CHAT_SESSION_PREFIX + sessionId, SessionStatusDetailDTO.class);
        } catch (Exception e) {
            log.error("获取会话详细信息缓存时发生异常，sessionId:{}", sessionId, e);
        }
        return null;
    }

    /**
     * 新增会话下的消息缓存
     *
     * @param sessionId  会话 id
     * @param messageDTO 消息 DTO
     */
    public void addMessageDOTToCache(Long sessionId, MessageDTO messageDTO) {
        try {
            // 会根据时间生成消息 id，所以用消息 id 作为分值来排序
            redisService.addMemberZSet(CHAT_ZSET_SESSION_PREFIX + sessionId, messageDTO, Long.parseLong(messageDTO.getMessageId()));
        } catch (Exception e) {
            log.error("新增会话下的消息缓存发生异常，sessionId:{}", sessionId, e);
        }
    }

    /**
     * 获取会话下的聊天记录集合
     *
     * @param sessionId 会话 id
     * @return 消息 DTO 集合 (注意：返回的消息列表是按时间倒序排列的)
     */
    public Set<MessageDTO> getMessageDTOSByCache(Long sessionId) {
        Set<MessageDTO> messageDTOSet = new HashSet<>();
        try {
            messageDTOSet = redisService.getCacheZSetDesc(CHAT_ZSET_SESSION_PREFIX + sessionId, new TypeReference<LinkedHashSet<MessageDTO>>() {
            });
            if (CollectionUtils.isEmpty(messageDTOSet)) {
                return new HashSet<>();
            }
        } catch (Exception e) {
            log.error("获取会话下的消息列表缓存发生异常，sessionId:{}", sessionId, e);
        }
        return messageDTOSet;
    }

    /**
     * 删除会话下的指定消息缓存
     *
     * @param sessionId 会话 id
     * @param messageId 消息 id
     */
    public void removeMessageDTOCache(Long sessionId, String messageId) {
        try {
            redisService.removeZSetByScore(CHAT_ZSET_SESSION_PREFIX + sessionId, Long.parseLong(messageId), Long.parseLong(messageId));
        } catch (Exception e) {
            log.error("删除会话下的指定消息缓存发生异常，sessionId:{}, messageId:{}", sessionId, messageId, e);
        }
    }
}
