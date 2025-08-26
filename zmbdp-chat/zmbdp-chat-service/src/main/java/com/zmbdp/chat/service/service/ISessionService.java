package com.zmbdp.chat.service.service;

import com.zmbdp.chat.service.domain.dto.SessionAddReqDTO;
import com.zmbdp.chat.service.domain.dto.SessionGetReqDTO;
import com.zmbdp.chat.service.domain.vo.SessionAddResVO;
import com.zmbdp.chat.service.domain.vo.SessionGetResVO;

/**
 * 会话服务接口
 *
 * @author 稚名不带撇
 */
public interface ISessionService {

    /**
     * 新建咨询会话
     *
     * @param sessionAddReqDTO 新建会话请求参数
     * @return 新建会话信息 DTO
     */
    SessionAddResVO add(SessionAddReqDTO sessionAddReqDTO);

    /**
     * 查询咨询会话
     *
     * @param sessionGetReqDTO 会话查询请求参数
     * @return 会话信息 DTO
     */
    SessionGetResVO get(SessionGetReqDTO sessionGetReqDTO);
}
