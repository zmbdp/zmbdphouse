package com.zmbdp.chat.service.controller;

import com.zmbdp.chat.service.domain.dto.MessageListReqDTO;
import com.zmbdp.chat.service.domain.vo.MessageVO;
import com.zmbdp.chat.service.service.IMessageService;
import com.zmbdp.common.domain.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消息控制器
 *
 * @author 稚名不带撇
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    /**
     * 消息服务接口
     */
    @Autowired
    private IMessageService messageService;

    /**
     * 获取历史聊天记录
     *
     * @param messageListReqDTO 获取消息列表 DTO
     * @return 消息列表
     */
    @PostMapping("/list")
    public Result<List<MessageVO>> list(@Validated @RequestBody MessageListReqDTO messageListReqDTO) {
        return Result.success(messageService.list(messageListReqDTO));
    }
}
