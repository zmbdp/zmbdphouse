package com.zmbdp.chat.service.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zmbdp.chat.service.domain.dto.*;
import com.zmbdp.chat.service.domain.entity.Message;
import com.zmbdp.chat.service.domain.entity.Session;
import com.zmbdp.chat.service.domain.enums.MessageStatusEnum;
import com.zmbdp.chat.service.domain.enums.MessageTypeEnum;
import com.zmbdp.chat.service.domain.vo.MessageVO;
import com.zmbdp.chat.service.mapper.MessageMapper;
import com.zmbdp.chat.service.mapper.SessionMapper;
import com.zmbdp.chat.service.service.ChatCacheService;
import com.zmbdp.chat.service.service.IMessageService;
import com.zmbdp.chat.service.service.SnowflakeIdService;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.core.utils.StringUtil;
import com.zmbdp.common.security.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息服务接口实现类
 *
 * @author 稚名不带撇
 */
@Slf4j
@Service
@RefreshScope
public class MessageServiceImpl implements IMessageService {

    /**
     * 消息表 Mapper
     */
    @Autowired
    private MessageMapper messageMapper;

    /**
     * 会话表 Mapper
     */
    @Autowired
    private SessionMapper sessionMapper;

    /**
     * 雪花算法 ID 生成服务
     */
    @Autowired
    private SnowflakeIdService snowflakeIdService;

    /**
     * 聊天缓存服务
     */
    @Autowired
    private ChatCacheService chatCacheService;

    /**
     * token 服务
     */
    @Autowired
    private TokenService tokenService;

    /**
     * jwt 密钥
     */
    @Value("${jwt.token.secret}")
    private String secret;

    /**
     * 根据消息 id 获取消息信息
     *
     * @param messageId 消息 ID
     * @return 消息数据传输对象
     */
    @Override
    public MessageDTO get(Long messageId) {
        if (null == messageId) {
            return null;
        }

        // 查询 mysql
        Message message = messageMapper.selectById(messageId);
        if (null == message) {
            return null;
        }
        MessageDTO messageDTO = new MessageDTO();
        BeanCopyUtil.copyProperties(message, messageDTO);
        messageDTO.setMessageId(String.valueOf(message.getId()));
        return messageDTO;
    }

    /**
     * 新增一条消息
     *
     * @param reqDTO 消息请求参数
     * @return 是否添加成功
     */
    @Override
    public boolean add(MessageSendReqDTO reqDTO) {
        // 校验参数
        if (null == MessageTypeEnum.getByCode(reqDTO.getType())) {
            log.error("新增消息时，消息列表有误! type:{}", reqDTO.getType());
            return false;
        }

        // 校验 sessionId
        Session session = sessionMapper.selectById(reqDTO.getSessionId());
        if (null == session) {
            log.error("新增消息时，会话不存在！sessionId:{}", reqDTO.getSessionId());
            return false;
        }

        // 数据库：message
        // 缓存：sessionId 列表 sessionId [MessageDTO...]、会话详细信息 DTO、用户下的会话列表 userId [sessionId...]
        Message message = new Message();
        message.setId(
                null == reqDTO.getMessageId() ?
                        snowflakeIdService.nextId() : reqDTO.getMessageId()
        );
        BeanCopyUtil.copyProperties(reqDTO, message);
        message.setContent(
                StringUtil.isEmpty(reqDTO.getContent()) ?
                        "" : reqDTO.getContent()
        );
        message.setStatus(
                null == reqDTO.getStatus() ?
                        MessageStatusEnum.MESSAGE_UNREAD.getCode() : reqDTO.getStatus()
        );
        message.setVisited(
                null == reqDTO.getVisited() ?
                        MessageStatusEnum.MESSAGE_NOT_VISITED.getCode() : reqDTO.getVisited()
        );
        message.setCreateTime(Long.parseLong(reqDTO.getCreateTime()));
        messageMapper.insert(message);

        // 更新缓存: (1).session 列表 sessionId [MessageDTO...] (2).会话详细信息 DTO (3).用户下的会话列表 userId [sessionId...]
        MessageDTO messageDTO = new MessageDTO();
        BeanCopyUtil.copyProperties(message, messageDTO);
        messageDTO.setMessageId(String.valueOf(message.getId()));
        chatCacheService.addMessageDOTToCache(message.getSessionId(), messageDTO);

        // (2).会话详细信息 DTO
        // 先拿出会话详细信息 DTO
        SessionStatusDetailDTO sessionDTO = chatCacheService.getSessionDTOByCache(message.getSessionId());
        assert null != sessionDTO;
        // 设置对方消息的未浏览数。
        SessionStatusDetailDTO.UserInfo toUserInfo = sessionDTO.getToUser(reqDTO.getFromId());
        toUserInfo.setNotVisitedCount(toUserInfo.getNotVisitedCount() + 1);
        sessionDTO.setLastSessionTime(message.getCreateTime());
        sessionDTO.setLastMessageDTO(messageDTO);
        // 必须是卡片消息，才会设置该属性
        if (MessageTypeEnum.MESSAGE_CARD.getCode().equals(reqDTO.getType())) {
            Set<Long> houseIds = sessionDTO.getHouseIds();
            // 将卡片消息中的字符串转成 json 对象，然后根据 key 取值，拿到房源 id
            String houseId = JSONObject.parseObject(reqDTO.getContent()).getString("houseId");
            houseIds.add(Long.parseLong(houseId));
            sessionDTO.setHouseIds(houseIds);
        }
        chatCacheService.cacheSessionDTO(message.getSessionId(), sessionDTO);

        // (3).用户下的会话列表 userId [sessionId...] (两个用户都要更新)，权重是最后一条消息的时间戳
        chatCacheService.addUserSessionToCache(
                session.getUserId1(), session.getId(), sessionDTO.getLastSessionTime()
        );
        chatCacheService.addUserSessionToCache(
                session.getUserId2(), session.getId(), sessionDTO.getLastSessionTime()
        );
        return true;
    }

    /**
     * 获取历史聊天记录
     *
     * @param messageListReqDTO 获取消息列表 DTO
     * @return 消息列表
     */
    @Override
    public List<MessageVO> list(MessageListReqDTO messageListReqDTO) {
        // 从缓存中获取会话id下的消息全集合（倒序: 最新的消息在最前面）
        Set<MessageDTO> messageDTOSet = chatCacheService.getMessageDTOSByCache(messageListReqDTO.getSessionId());
        if (CollectionUtils.isEmpty(messageDTOSet)) {
            return List.of();
        }

        // 遍历联表，构造需要返回的结果
        List<MessageVO> resultList = new ArrayList<>();
        // 还需要获取的消息条数
        int curCount = messageListReqDTO.getCount();
        for (MessageDTO messageDTO : messageDTOSet) {
            // 遍历到传入的最后一条消息，需要判断下是否需要获取这个消息
            if (
                    messageDTO.getMessageId().equalsIgnoreCase(messageListReqDTO.getLastMessageId()) &&
                            messageListReqDTO.getNeedCurMessage()
            ) {
                MessageVO messageVO = new MessageVO();
                BeanCopyUtil.copyProperties(messageDTO, messageVO);
                resultList.add(messageVO);
                curCount--;
                // 如果当前消息 id（时间戳） 小于最后一条消息 id，说明在它之前，就要获取历史记录了
            } else if (0 > messageDTO.getMessageId().compareTo(messageListReqDTO.getLastMessageId())) {
                // 获取历史消息
                MessageVO messageVO = new MessageVO();
                BeanCopyUtil.copyProperties(messageDTO, messageVO);
                resultList.add(messageVO);
                curCount--;
            }
            if (curCount <= 0) {
                break;
            }
        }
        // 由于缓存中的消息是倒序的，最新的消息在最前面
        // 那么遍历的时候，往 resultList add 时也是最新消息在最前面
        // 需要逆置
        Collections.reverse(resultList);
        return resultList;
    }

    /**
     * 批量更新消息访问状态
     *
     * @param reqDTO 更新消息访问状态 DTO
     */
    @Override
    public void batchVisited(MessageVisitedReqDTO reqDTO) {
        // 查询对方用户 id
        Long loginUserId = tokenService.getLoginUser(secret).getUserId();
        Session session = sessionMapper.selectById(reqDTO.getSessionId());
        // 当前登录用户的 id 是自己，判断哪个 id 不相等哪个就是对方的，要把会话消息列表中，对方消息下面的状态改成已读
        Long otherUserId = loginUserId.equals(session.getUserId1()) ? session.getUserId2() : session.getUserId1();

        // 修改对方用户消息的访问状态（Mysql）
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getSessionId, reqDTO.getSessionId())
                .eq(Message::getFromId, otherUserId)
                .eq(Message::getVisited, MessageStatusEnum.MESSAGE_NOT_VISITED.getCode())
                .set(Message::getVisited, MessageStatusEnum.MESSAGE_VISITED.getCode())
        );

        // 修改对方用户消息的访问状态 （Redis）
        // 会话-消息列表
        Set<MessageDTO> messageDTOS = chatCacheService.getMessageDTOSByCache(reqDTO.getSessionId());
        if (CollectionUtils.isEmpty(messageDTOS)) {
            return;
        }
        // 遍历所有的消息，更新未浏览状态为已浏览
        for (MessageDTO messageDTO : messageDTOS) {
            // 自己的消息不处理
            if (messageDTO.getFromId().equals(loginUserId)) {
                continue;
            }

            // 当遍历到的消息为已浏览，说明以前的消息都时已浏览，直接 break 就行了
            if (MessageStatusEnum.MESSAGE_VISITED.getCode().equals(messageDTO.getVisited())) {
                break;
            }

            // 需要更新浏览状态,先删除再新增
            messageDTO.setVisited(MessageStatusEnum.MESSAGE_VISITED.getCode());
            chatCacheService.removeMessageDTOCache(messageDTO.getSessionId(), messageDTO.getMessageId());
            chatCacheService.addMessageDOTToCache(messageDTO.getSessionId(), messageDTO);
        }
        // 修改会话详情缓存:
        // 1. 登录用户记录的对方消息未浏览数
        // 2. 最后一条聊天消息（访问状态）
        SessionStatusDetailDTO sessionDTO = chatCacheService.getSessionDTOByCache(reqDTO.getSessionId());
        SessionStatusDetailDTO.UserInfo userInfo = sessionDTO.getFromUser(loginUserId);
        userInfo.setNotVisitedCount(0);
        sessionDTO.setLastMessageDTO(messageDTOS.iterator().next());
        chatCacheService.cacheSessionDTO(sessionDTO.getSessionId(), sessionDTO);
    }

    /**
     * 更新消息已读状态（目前只有语音）
     *
     * @param reqDTO 更新消息已读状态 DTO
     */
    @Override
    public void batchRead(MessageReadReqDTO reqDTO) {
        // 修改 MySql
        List<Long> messageIds = reqDTO.getMessageIds().stream()
                .map(Long::valueOf)
                .toList();
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .in(Message::getId, messageIds)
                .set(Message::getStatus, MessageStatusEnum.MESSAGE_READ.getCode())
        );

        // 修改 Redis
        Set<MessageDTO> messageDTOS = chatCacheService.getMessageDTOSByCache(reqDTO.getSessionId());
        if (CollectionUtils.isEmpty(messageDTOS)) {
            return;
        }

        int count = reqDTO.getMessageIds().size();
        for (MessageDTO messageDTO : messageDTOS) {
            if (reqDTO.getMessageIds().contains(messageDTO.getMessageId())) {
                messageDTO.setStatus(MessageStatusEnum.MESSAGE_READ.getCode());
                chatCacheService.removeMessageDTOCache(messageDTO.getSessionId(), messageDTO.getMessageId());
                chatCacheService.addMessageDOTToCache(messageDTO.getSessionId(), messageDTO);
                count--;
            }
            if (count <= 0) {
                break;
            }
        }

        // 修改会话详情缓存:
        // 1. 登录用户记录的对方消息未浏览数
        // 2. 最后一条聊天消息（访问状态）
        SessionStatusDetailDTO sessionDTO = chatCacheService.getSessionDTOByCache(reqDTO.getSessionId());
        sessionDTO.setLastMessageDTO(messageDTOS.iterator().next());
        chatCacheService.cacheSessionDTO(sessionDTO.getSessionId(), sessionDTO);
    }
}
