package com.zmbdp.chat.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zmbdp.admin.api.appuser.domain.dto.AppUserDTO;
import com.zmbdp.admin.api.appuser.domain.vo.AppUserVO;
import com.zmbdp.admin.api.appuser.feign.AppUserApi;
import com.zmbdp.chat.service.domain.dto.SessionAddReqDTO;
import com.zmbdp.chat.service.domain.dto.SessionGetReqDTO;
import com.zmbdp.chat.service.domain.dto.SessionStatusDetailDTO;
import com.zmbdp.chat.service.domain.entity.Session;
import com.zmbdp.chat.service.domain.vo.MessageVO;
import com.zmbdp.chat.service.domain.vo.SessionAddResVO;
import com.zmbdp.chat.service.domain.vo.SessionGetResVO;
import com.zmbdp.chat.service.mapper.SessionMapper;
import com.zmbdp.chat.service.service.ChatCacheService;
import com.zmbdp.chat.service.service.ISessionService;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.common.domain.domain.ResultCode;
import com.zmbdp.common.domain.exception.ServiceException;
import com.zmbdp.common.security.domain.dto.LoginUserDTO;
import com.zmbdp.common.security.service.TokenService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 会话服务实现类
 *
 * @author 稚名不带撇
 */
@Service
@RefreshScope
public class SessionServiceImpl implements ISessionService {

    /**
     * 会话 Mapper
     */
    @Autowired
    private SessionMapper sessionMapper;

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
     * 用户服务 API
     */
    @Autowired
    private AppUserApi appUserApi;

    /**
     * JWT 密钥
     */
    @Value("${jwt.token.secret}")
    private String secret;

    /**
     * 新建咨询会话
     *
     * @param sessionAddReqDTO 新建会话请求参数
     * @return 新建会话信息 DTO
     */
    @Override
    public SessionAddResVO add(SessionAddReqDTO sessionAddReqDTO) {
        // 获取当前登录用户 id
        Long loginUserId = tokenService.getLoginUser(secret).getUserId();

        // 先排序，始终让 userId1 小于 userId2
        Long userId1 = Math.min(sessionAddReqDTO.getUserId1(), sessionAddReqDTO.getUserId2());
        Long userId2 = Math.max(sessionAddReqDTO.getUserId1(), sessionAddReqDTO.getUserId2());

        // 然后校验会话是否存在
        Session session = sessionMapper.selectOne(new LambdaQueryWrapper<Session>()
                .eq(Session::getUserId1, userId1)
                .eq(Session::getUserId2, userId2)
        );

        // 如果不存在就创建新会话到 mysql, redis 中
        if (session == null) {
            Result<List<AppUserVO>> appUserVOList = appUserApi.list(Arrays.asList(userId1, userId2));
            if (
                    null == appUserVOList ||
                    appUserVOList.getCode() != ResultCode.SUCCESS.getCode() ||
                    CollectionUtils.isEmpty(appUserVOList.getData()) ||
                    appUserVOList.getData().size() != 2 // 确保查到两个用户
            ) {
                throw new ServiceException("新增会话时，用户不存在！");
            }

            session = new Session();
            session.setUserId1(userId1);
            session.setUserId2(userId2);
            sessionMapper.insert(session);
            // 然后再添加到 redis 中

            Map<Long, AppUserDTO> userMap = appUserVOList.getData().stream()
                    .map(appUserVO -> {
                        AppUserDTO appUserDTO = new AppUserDTO();
                        BeanCopyUtil.copyProperties(appUserVO, appUserDTO);
                        return appUserDTO;
                    }).collect(Collectors.toMap(AppUserDTO::getUserId, Function.identity()));

            SessionStatusDetailDTO sessionDTO = new SessionStatusDetailDTO();
            sessionDTO.setSessionId(session.getId());

            // 设置 用户1 的信息
            SessionStatusDetailDTO.UserInfo userInfo1 = new SessionStatusDetailDTO.UserInfo();
            userInfo1.setUser(userMap.get(userId1));
            sessionDTO.setUser1(userInfo1);

            // 设置 用户2 的信息
            SessionStatusDetailDTO.UserInfo userInfo2 = new SessionStatusDetailDTO.UserInfo();
            userInfo2.setUser(userMap.get(userId2));
            sessionDTO.setUser2(userInfo2);
            // 最后存入 redis
            chatCacheService.cacheSessionDTO(session.getId(), sessionDTO);
        }
        // 最后直接查 redis，拷贝类型返回会话信息 DTO
        SessionStatusDetailDTO sessionDTO = chatCacheService.getSessionDTOByCache(session.getId());
        assert null != sessionDTO;
        SessionAddResVO resVO = new SessionAddResVO();
        resVO.setSessionId(session.getId());
        resVO.setLoginUser(sessionDTO.getFromUser(loginUserId).getUser().convertToVO());
        resVO.setOtherUser(sessionDTO.getToUser(loginUserId).getUser().convertToVO());
        return resVO;
    }

    /**
     * 查询咨询会话
     *
     * @param sessionGetReqDTO 会话查询请求参数
     * @return 会话信息 DTO
     */
    @Override
    public SessionGetResVO get(SessionGetReqDTO sessionGetReqDTO) {
        SessionGetResVO resVO = new SessionGetResVO();

        // 先排序
        Long userId1 = Math.min(sessionGetReqDTO.getUserId1(), sessionGetReqDTO.getUserId2());
        Long userId2 = Math.max(sessionGetReqDTO.getUserId1(), sessionGetReqDTO.getUserId2());

        // 查询会话是否存在
        Session session = sessionMapper.selectOne(new LambdaQueryWrapper<Session>()
                .eq(Session::getUserId1, userId1)
                .eq(Session::getUserId2, userId2)
        );
        // 不存在，返回空
        if (session == null) {
            return resVO;
        }

        // 存在，查缓存，构造返回
        SessionStatusDetailDTO sessionDTO = chatCacheService.getSessionDTOByCache(session.getId());
        if (null == sessionDTO) {
            throw new ServiceException("聊天会话 id 不一致");
        }

        // 拷贝属性
        resVO.setSessionId(session.getId());
        // 最后消息是否存在，存在的话就拷贝过去
        if (null != sessionDTO.getLastMessageDTO()) {
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(sessionDTO.getLastMessageDTO(), messageVO);
            resVO.setLastMessageVO(messageVO);
        }
        // 最后会话时间是否存在，存在的话就拷贝过去
        if (null != sessionDTO.getLastSessionTime()) {
            resVO.setLastSessionTime(sessionDTO.getLastSessionTime());
        }

        // 未浏览数：当前登录用户未浏览对方用户的消息数，存在自己的用户信息中
        LoginUserDTO loginUser = tokenService.getLoginUser(secret);
        if (loginUser == null) {
            throw new ServiceException("用户未登录或登录已过期");
        }
        Long loginUserId = loginUser.getUserId();
        if (loginUserId == null) {
            throw new ServiceException("用户未登录或登录已过期");
        }
        resVO.setNotVisitedCount(sessionDTO.getFromUser(loginUserId).getNotVisitedCount());
        resVO.setOtherUser(sessionDTO.getToUser(loginUserId).getUser().convertToVO());
        return resVO;
    }


}
