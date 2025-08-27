package com.zmbdp.chat.service.service;

import com.zmbdp.chat.service.domain.dto.SessionAddReqDTO;
import com.zmbdp.chat.service.domain.dto.SessionGetReqDTO;
import com.zmbdp.chat.service.domain.dto.SessionHouseReqDTO;
import com.zmbdp.chat.service.domain.dto.SessionListReqDTO;
import com.zmbdp.chat.service.domain.vo.SessionAddResVO;
import com.zmbdp.chat.service.domain.vo.SessionGetResVO;

import java.util.List;

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

    /**
     * 查询咨询会话列表
     *
     * @param sessionListReqDTO 会话列表查询请求参数
     * @return 会话列表 DTO 列表
     */
    List<SessionGetResVO> list(SessionListReqDTO sessionListReqDTO);

    /**
     * 查看会话下是否聊过某房源
     *
     * @param sessionHouseReqDTO 会话查看房源请求参数
     * @return 是否聊过该房源
     */
    Boolean hasHouse(SessionHouseReqDTO sessionHouseReqDTO);
}
