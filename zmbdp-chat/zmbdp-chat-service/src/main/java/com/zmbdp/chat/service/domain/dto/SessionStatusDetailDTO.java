package com.zmbdp.chat.service.domain.dto;

import com.zmbdp.admin.api.appuser.domain.dto.AppUserDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * 会话状态详情
 *
 * @author 稚名不带撇
 */
@Data
@Slf4j
public class SessionStatusDetailDTO {

    /**
     * 会话id
     */
    private Long sessionId;

    /**
     * 用户1 信息
     */
    private UserInfo user1;

    /**
     * 用户2 信息
     */
    private UserInfo user2;

    /**
     * 会话最后时间(毫秒时间戳)
     */
    private Long lastSessionTime;

    /**
     * 聊过的房源id列表
     */
    private Set<Long> houseIds = new HashSet<>();

    /**
     * 最后一条消息
     */
    private MessageDTO lastMessageDTO;

    /**
     * 根据当前登录用户返回发送方用户信息
     *
     * @param loginUserId 登录的用户id
     * @return 会话中发送方用户信息
     */
    public UserInfo getFromUser(Long loginUserId) {
        if (null == loginUserId) {
            log.error("根据登录的用户id获取会话中发送方信息失败，原因：登录的用户id不存在！");
            return null;
        }
        if (null == user1 || null == user1.getUser()
                || null == user2 || null == user2.getUser()) {
            log.error("根据登录的用户id获取会话中发送方信息失败，原因：用户信息不存在！");
            return null;
        }
        return loginUserId.equals(user1.getUser().getUserId()) ? user1 : user2;
    }

    /**
     * 根据当前登录用户返回发送方用户信息
     *
     * @param loginUserId 登录的用户id
     * @return 会话中接收方用户信息
     */
    public UserInfo getToUser(Long loginUserId) {
        if (null == loginUserId) {
            log.error("根据登录的用户id获取会话中发送方信息失败，原因：登录的用户id不存在！");
            return null;
        }
        if (null == user1 || null == user1.getUser()
                || null == user2 || null == user2.getUser()) {
            log.error("根据登录的用户id获取会话中发送方信息失败，原因：用户信息不存在！");
            return null;
        }
        return loginUserId.equals(user1.getUser().getUserId()) ? user2 : user1;
    }

    @Data
    public static class UserInfo {

        /**
         * 用户信息
         */
        private AppUserDTO user;

        /**
         * 对于对方消息的未浏览数
         */
        private Integer notVisitedCount = 0;
    }
}