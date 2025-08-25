package com.zmbdp.chat.service.domain.vo;

import com.zmbdp.admin.api.appuser.domain.vo.AppUserVO;
import lombok.Data;

/**
 * 会话添加结果VO
 *
 * @author 稚名不带撇
 */
@Data
public class SessionAddResVO {

    /**
     * 会话Id
     */
    private Long sessionId;

    /**
     * 登录用户
     */
    private AppUserVO loginUser;

    /**
     * 对方用户
     */
    private AppUserVO otherUser;
}