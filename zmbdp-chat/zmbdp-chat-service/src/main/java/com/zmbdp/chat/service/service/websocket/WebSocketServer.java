package com.zmbdp.chat.service.service.websocket;

import com.zmbdp.chat.service.config.JwtConfig;
import com.zmbdp.chat.service.config.ServerEncoder;
import com.zmbdp.chat.service.config.SpringContextHolder;
import com.zmbdp.chat.service.config.WebSocketConfig;
import com.zmbdp.chat.service.domain.dto.MessageSendReqDTO;
import com.zmbdp.chat.service.domain.dto.WebSocketDTO;
import com.zmbdp.chat.service.domain.enums.MessageStatusEnum;
import com.zmbdp.chat.service.domain.enums.WebSocketDataTypeEnum;
import com.zmbdp.chat.service.mq.sender.MessageProduce;
import com.zmbdp.chat.service.service.SnowflakeIdService;
import com.zmbdp.common.core.utils.JsonUtil;
import com.zmbdp.common.core.utils.StringUtil;
import com.zmbdp.common.domain.domain.ResultCode;
import com.zmbdp.common.domain.exception.ServiceException;
import com.zmbdp.common.security.domain.dto.LoginUserDTO;
import com.zmbdp.common.security.service.TokenService;
import com.zmbdp.common.security.utils.SecurityUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器端的 EndPoint
 *
 * @author 稚名不带撇
 */
@Slf4j
@Service
@ServerEndpoint(
        value = "/websocket",
        configurator = WebSocketConfig.class,
        encoders = {ServerEncoder.class}
)
public class WebSocketServer {

    /**
     * 存放服务区和每个客户端对应的WebSocket对象。<p>
     * 建立连接之后去设值，断开连接之后需要删除
     */
    private static ConcurrentHashMap<Long, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();

    /**
     * 令牌服务<p>
     * 不能使用 @Autowired 或 @Resource<p>
     * 因为 ws 是通过 WebSocketConfig.getEndpointInstance() 方法来获取每个连接对应的调用对象<p>
     * 而 getEndpointInstance 默认是通过反射来构造的，而不是 Spring 容器获取连接对象<p>
     * 如果不是静态属性，WebSocket 实例是 由容器自己反射创建的，根本不会经过 Spring 注入过程，所以那个字段永远都是 null。
     */
    private static TokenService tokenService;

    /**
     * 消息生产者服务
     */
    private static MessageProduce messageProduce;

    /**
     * 雪花 ID 生成器
     */
    private static SnowflakeIdService snowflakeIdService;

    /**
     * 连接会话
     */
    private Session session;

    /**
     * 连接用户 ID
     */
    private Long userId;


    /**
     * 令牌服务注入<p>
     * 相当于就是说，ws 根本拿不到 spring 里面的 bean<p>
     * 通过 @Autowired 的方法也就是让 spring 帮我们发现并执行这个方法，然后把这个方法的参数注入到 ws 中<p>
     * 然后 tokenService 设置为静态属性，可以共享，ws 就能拿到了
     *
     * @param tokenService 令牌服务
     *                     静态属性，可以共享
     */
    @Autowired
    public void setTokenService(TokenService tokenService) {
        WebSocketServer.tokenService = tokenService;
    }

    @Autowired
    public void setSnowflakeIdService(SnowflakeIdService snowflakeIdService) {
        WebSocketServer.snowflakeIdService = snowflakeIdService;
    }

    @Autowired
    public void setMessageProduce(MessageProduce messageProduce) {
        WebSocketServer.messageProduce = messageProduce;
    }

    /**
     * 从 Spring 容器中获取 JwtConfig 配置<p>
     * 因为 secret 是变量，所以不太能用静态内存共享这种方式，只能每次用的时候都从 spring 容器里面获取一下
     *
     * @return JwtConfig 配置
     */
    private JwtConfig getJwtConfig() {
        return SpringContextHolder.getBean(JwtConfig.class);
    }

    /**
     * 连接建立成功调用的方法
     *
     * @param session 会话
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        String secret = getJwtConfig().getSecret();
        try {
            log.info("用户连接成功");
            // 先获取 token
            String token = String.valueOf(session.getUserProperties().get("Authorization"));
            token = SecurityUtil.replaceTokenPrefix(token);
            if (StringUtil.isBlank(token)) {
                throw new ServiceException("没有传递用户token！", ResultCode.INVALID_PARA.getCode());
            }
            // 根据 token 获取用户信息
            LoginUserDTO loginUser = tokenService.getLoginUser(token, secret);
            if (null == loginUser || null == loginUser.getUserId()) {
                throw new ServiceException("用户token有误！", ResultCode.INVALID_PARA.getCode());
            }

            // 设置 ws 属性
            this.session = session;
            this.userId = loginUser.getUserId();

            // 加入 webSocketMap 管理，方便其他服务器拿取数据
            webSocketMap.put(this.userId, this);
            log.info("用户 [{}] 已经连接", userId);
        } catch (Exception e) {
            log.error("用户连接失败：{}; 即将关闭连接", e.toString());
            session.close();
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() throws IOException {
        if (userId != null && webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
        }
        log.info("用户 [{}] 已经关闭连接", userId);
        this.session = null;
        this.userId = null;
    }

    /**
     * 连接发生异常后调用的方法
     *
     * @param session 会话
     * @param error   异常数据
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.error("websocket连接发生异常，session: {}, error: {}", session, String.valueOf(error));
        this.session = null;
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message) {
        try {
            log.info("收到来自客户端的消息：{}", message);

            // 处理消息，JSON 转对象
            WebSocketDTO<?> webSocketDTO = JsonUtil.jsonToClass(message, WebSocketDTO.class);
            if (null == webSocketDTO) {
                log.error("webSocket 不支持的协议：{}", message);
                return;
            }

            handleMessage(webSocketDTO.getType(), webSocketDTO.getData());
        } catch (Exception e) {
            log.error("发送消息异常：{}", e.toString());
        }
    }


    /**
     * 处理消息
     *
     * @param type 消息类型
     * @param data 消息数据
     */
    private <T> void handleMessage(String type, T data) {
        WebSocketDataTypeEnum typeEnum = WebSocketDataTypeEnum.getByType(type);

        if (typeEnum == null) {
            handleUnknownMessage(type);
            return;
        }
        switch (typeEnum) {
            case TEXT:
                // 处理文本消息
                handleTextMessage(String.valueOf(data));
                break;
            case HEART_BEAT:
                // 处理心跳消息
                handleHeartBeatMessage();
                break;
            case CHAT:
                // 处理聊天消息
                handleChatMessage(String.valueOf(data));
                break;
            default:
                // 未知消息类型
                handleUnknownMessage(type);
                break;
        }
    }

    /**
     * 处理文本消息
     *
     * @param data 消息内容
     */
    private void handleTextMessage(String data) {
        try {
            Thread.sleep(3000);
            log.info("收到来自客户端的文本消息：{}", data);
            sendMessage(new WebSocketDTO<>(WebSocketDataTypeEnum.TEXT.getType(), "服务器已收到消息：" + data));
        } catch (Exception e) {
            log.error("处理文本消息异常：{}", e.toString());
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartBeatMessage() {
        try {
            log.info("收到心跳消息");
            sendMessage(new WebSocketDTO<>(WebSocketDataTypeEnum.HEART_BEAT.getType(), "pong"));
        } catch (Exception e) {
            log.error("处理心跳消息异常：{}", e.toString());
        }
    }


    /**
     * 处理聊天消息
     *
     * @param data 消息内容
     */
    private void handleChatMessage(String data) {
        try {
            // 反序列化成咨询聊天消息
            MessageSendReqDTO messageSendReqDTO = JsonUtil.jsonToClass(data, MessageSendReqDTO.class);
            if (null == messageSendReqDTO) {
                throw new ServiceException("聊天消息为空！");
            }

            // 广播咨询聊天消息
            messageSendReqDTO.setMessageId(snowflakeIdService.nextId());
            messageSendReqDTO.setStatus(MessageStatusEnum.MESSAGE_UNREAD.getCode());
            messageSendReqDTO.setVisited(MessageStatusEnum.MESSAGE_NOT_VISITED.getCode());
            messageProduce.sendMessage(messageSendReqDTO);

        } catch (Exception e) {
            log.error("生产者发送消息异常，data:{}", data, e);
        }
    }

    /**
     * 处理未知消息类型
     *
     * @param type 消息数据
     */
    private void handleUnknownMessage(String type) {
        log.error("无效的消息类型, 无法处理：{}", type);
    }

    /**
     * 给当前会话连接推送消息
     *
     * @param webSocketDTO 消息数据
     */
    private void sendMessage(WebSocketDTO<?> webSocketDTO) {
        try {
            this.session.getBasicRemote().sendObject(webSocketDTO);
        } catch (Exception e) {
            log.error("ws 推送消息失败！webSocketDTO：{}", JsonUtil.classToJson(webSocketDTO), e);
        }
    }

    /**
     * 给指定用户推送消息（这里的用户是当前服务器自己管理的 session）
     *
     * @param userId 用户 ID
     * @param webSocketDTO 消息数据
     */
    public static void sendMessage(Long userId, WebSocketDTO<?> webSocketDTO) {
        if (!webSocketMap.containsKey(userId)) {
            // 无法推送，丢弃
            return;
        }
        webSocketMap.get(userId).sendMessage(webSocketDTO);
        log.info("消息转发成功:{}", JsonUtil.classToJson(webSocketDTO));
    }
}

/// **
// * 服务器端的 EndPoint
// * 直接将 websocket 交给 spring 管理就这么写
// * @author 稚名不带撇
// */
//@Slf4j
//@Service
//@RefreshScope
//@Scope("prototype") // 每个连接都创建一个新的实例
//@ServerEndpoint(
//        value = "/websocket",
//        configurator = WebSocketConfig.class,
//        encoders = {ServerEncoder.class}
//)
//public class WebSocketServer {
//
//    /**
//     * 存放服务区和每个客户端对应的WebSocket对象。<p>
//     * 建立连接之后去设值，断开连接之后需要删除
//     */
//    private static ConcurrentHashMap<Long, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
//
//    /**
//     * 令牌服务<p>
//     * 不能使用 @Autowired 或 @Resource<p>
//     * 因为 ws 是通过 WebSocketConfig.getEndpointInstance() 方法来获取每个连接对应的调用对象<p>
//     * 而 getEndpointInstance 默认是通过反射来构造的，而不是 Spring 容器获取连接对象<p>
//     * 如果不是静态属性，WebSocket 实例是 由容器自己反射创建的，根本不会经过 Spring 注入过程，所以那个字段永远都是 null。
//     */
//    @Autowired
//    private TokenService tokenService;
//
//    /**
//     * 连接会话
//     */
//    private Session session;
//
//    @Value("${jwt.token.secret}")
//    private String secret;
//
//    /**
//     * 连接用户 ID
//     */
//    private Long userId;
//}

