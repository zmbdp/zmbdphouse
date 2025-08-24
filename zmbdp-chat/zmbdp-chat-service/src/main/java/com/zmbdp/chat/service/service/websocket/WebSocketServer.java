package com.zmbdp.chat.service.service.websocket;

import com.zmbdp.chat.service.config.WebSocketConfig;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 服务器端的 EndPoint
 *
 * @author 稚名不带撇
 */
@Slf4j
@Component
@ServerEndpoint(value = "/websocket", configurator = WebSocketConfig.class)
public class WebSocketServer {

    /**
     * 连接会话
     */
    private Session session;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        log.info("用户连接成功");
        this.session = session;
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() throws IOException {
        log.info("用户退出");
        this.session = null;
    }

    /**
     * 连接发生异常后调用的方法
     *
     * @param session 会话
     * @param error   异常数据
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.error("websocket连接发生异常，session: {}, error: {}", session, error.toString());
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
            Thread.sleep(3000);
            log.info("收到来自客户端的消息：{}", message);
            this.session.getBasicRemote().sendText("消息已收到: " + message);
        } catch (Exception e) {
            log.error("发送消息异常：{}", e.toString());
        }
    }
}
