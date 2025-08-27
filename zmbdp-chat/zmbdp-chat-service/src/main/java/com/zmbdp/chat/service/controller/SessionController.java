package com.zmbdp.chat.service.controller;


import com.zmbdp.chat.service.domain.dto.SessionAddReqDTO;
import com.zmbdp.chat.service.domain.dto.SessionGetReqDTO;
import com.zmbdp.chat.service.domain.dto.SessionHouseReqDTO;
import com.zmbdp.chat.service.domain.dto.SessionListReqDTO;
import com.zmbdp.chat.service.domain.vo.SessionAddResVO;
import com.zmbdp.chat.service.domain.vo.SessionGetResVO;
import com.zmbdp.chat.service.service.ISessionService;
import com.zmbdp.common.domain.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会话控制器
 *
 * @author 稚名不带撇
 */
@RestController
@RequestMapping("/session")
public class SessionController {

    /**
     * 会话服务
     */
    @Autowired
    private ISessionService sessionService;

    /**
     * 新建咨询会话
     *
     * @param sessionAddReqDTO 新建会话请求参数
     * @return 新建会话信息 DTO
     */
    @PostMapping("/add")
    public Result<SessionAddResVO> add(@Validated @RequestBody SessionAddReqDTO sessionAddReqDTO) {
        return Result.success(sessionService.add(sessionAddReqDTO));
    }

    /**
     * 查询咨询会话
     *
     * @param sessionGetReqDTO 会话查询请求参数
     * @return 会话信息 DTO
     */
    @PostMapping("/get")
    public Result<SessionGetResVO> get(@Validated @RequestBody SessionGetReqDTO sessionGetReqDTO ) {
        return Result.success(sessionService.get(sessionGetReqDTO));
    }

    /**
     * 查询咨询会话列表
     *
     * @param sessionListReqDTO 会话列表查询请求参数
     * @return 会话列表 DTO 列表
     */
    @PostMapping("/list")
    public Result<List<SessionGetResVO>> list(@Validated @RequestBody SessionListReqDTO sessionListReqDTO ) {
        return Result.success(sessionService.list(sessionListReqDTO));
    }

    /**
     * 查看会话下是否聊过某房源
     *
     * @param sessionHouseReqDTO 会话查看房源请求参数
     * @return 是否聊过该房源
     */
    @PostMapping("/has_house")
    public Result<Boolean> hasHouse(@Validated @RequestBody SessionHouseReqDTO sessionHouseReqDTO) {
        return Result.success(sessionService.hasHouse(sessionHouseReqDTO));
    }
}
