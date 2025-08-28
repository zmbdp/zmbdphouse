package com.zmbdp.chat.service.service;

import com.zmbdp.chat.service.domain.dto.MessageDTO;
import com.zmbdp.chat.service.domain.dto.MessageListReqDTO;
import com.zmbdp.chat.service.domain.dto.MessageSendReqDTO;
import com.zmbdp.chat.service.domain.vo.MessageVO;

import java.util.List;

/**
 * 消息服务接口
 *
 * @author 稚名不带撇
 */
public interface IMessageService {

    /**
     * 根据消息 id 获取消息信息
     *
     * @param messageId 消息 ID
     * @return 消息数据传输对象
     */
    MessageDTO get(Long messageId);

    /**
     * 新增一条消息
     *
     * @param reqDTO 消息请求参数
     * @return 是否添加成功
     */
    boolean add(MessageSendReqDTO reqDTO);

    /**
     * 获取历史聊天记录
     *
     * @param messageListReqDTO 获取消息列表 DTO
     * @return 消息列表
     */
    List<MessageVO> list(MessageListReqDTO messageListReqDTO);
}
