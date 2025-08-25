package com.zmbdp.chat.service.config;

import com.zmbdp.common.core.utils.JsonUtil;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

/**
 * 定义将 Java 对象编码为 WebSocket 协议中的文本消息
 *
 * @author 稚名不带撇
 */
public class ServerEncoder implements Encoder.Text<Object> {

    /**
     * 销毁编码器
     */
    @Override
    public void destroy() {
        // 清理资源，如关闭文件等
    }

    /**
     * 初始化编码器
     *
     * @param arg0 端点配置
     */
    @Override
    public void init(EndpointConfig arg0) {
        // 初始化编码器，可以读取配置参数
    }

    /**
     * 编码对象为 JSON 字符串
     *
     * @param obj 要编码的对象
     * @return 编码后的 JSON 字符串
     */
    @Override
    public String encode(Object obj) {
        return JsonUtil.classToJson(obj);
    }
}