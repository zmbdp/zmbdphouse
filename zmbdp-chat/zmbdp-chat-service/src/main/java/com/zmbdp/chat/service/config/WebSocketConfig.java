package com.zmbdp.chat.service.config;

import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 配置 WebSocket
 *
 * @author 稚名不带撇
 */
@Configuration
public class WebSocketConfig extends ServerEndpointConfig.Configurator {

    /**
     * 这个类注册每个加了 @ServerEndpoint 的 spring bean节点，算是 spring 整合 websocket的一个体现，
     * 没有的话会报 404
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

//    /**
//     * 建立握手时，连接之前的操作，可以获取到源信息。
//     *
//     * @param sec      ServerEndpointConfig
//     * @param request  HttpServletRequest
//     * @param response HttpServletResponse
//     */
//    @Override
//    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
//        // 设置用户信息
//        Map<String, List<String>> headers = request.getHeaders();
//        List<String> authorization = headers.get("Authorization");
//        if (CollectionUtils.isEmpty(authorization)) {
//            // 建立连接时，请求头中的用户信息为空，无法通过校验。
//            // 管理连接要求 Authorization 必传
//            throw new RuntimeException("请求头不符合要求，缺少 Authorization 信息");
//        }
//
//        final Map<String, Object> userProperties = sec.getUserProperties();
//        userProperties.put("Authorization", authorization.get(0));
//    }
}
