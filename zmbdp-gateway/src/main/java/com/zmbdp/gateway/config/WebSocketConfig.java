package com.zmbdp.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.socket.client.TomcatWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy;

/**
 * WebSocket 配置类
 *
 * @author 稚名不带撇
 */
@Configuration
public class WebSocketConfig {

    /**
     * <pre>
     * 创建一个 TomcatWebSocketClient 实例，并注册为 Spring Bean
     * TomcatWebSocketClient 作用：
     * 1. 用于创建 WebSocket 连接
     * 2. 用于发送和接收 WebSocket 消息
     * 3. 用于处理 WebSocket 连接的关闭
     * 4. 用于处理 WebSocket 连接的异常
     * 5. 用于处理 WebSocket 连接的超时
     * 6. 用于处理 WebSocket 连接的 ping-pong 消息
     * 7. 用于处理 WebSocket 连接的二进制消息
     * 8. 用于处理 WebSocket 连接的文本消息
     * 9. 用于处理 WebSocket 连接的 JSON 消息
     * 10. 用于处理 WebSocket 连接的 XML 消息
     * 11. 用于处理 WebSocket 连接的自定义消息
     * </pre>
     * @return TomcatWebSocketClient 实例
     */
    @Bean
    @Primary
    WebSocketClient tomcatWebSocketClient() {
        return new TomcatWebSocketClient();
    }

    /**
     * <pre>
     * 创建一个 TomcatRequestUpgradeStrategy 实例，并注册为 Spring Bean
     * TomcatRequestUpgradeStrategy 作用：
     * 1. 用于升级 HTTP 连接为 WebSocket 连接
     * 2. 用于处理 WebSocket 连接的升级请求
     * 3. 用于处理 WebSocket 连接的升级响应
     * </pre>
     * @return TomcatRequestUpgradeStrategy 实例
     */
    @Bean
    @Primary
    public RequestUpgradeStrategy requestUpgradeStrategy() {
        return new TomcatRequestUpgradeStrategy();
    }
}
