package com.zmbdp.chat.service.domain.vo;

import com.zmbdp.admin.api.appuser.domain.vo.AppUserVO;
import lombok.Data;

/**
 * 会话获取结果VO
 *
 * @author 稚名不带撇
 */
@Data
public class SessionGetResVO {

    /**
     * 会话Id
     */
    private Long sessionId;

    /**
     * 最后一条消息信息
     */
    private MessageVO lastMessageVO;

    /**
     * 最后会话时间
     */
    private Long lastSessionTime;

    /**
     * 消息未浏览数
     */
    private Integer notVisitedCount;

    /**
     * 对方信息
     */
    private AppUserVO otherUser;
}