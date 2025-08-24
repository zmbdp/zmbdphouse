package com.zmbdp.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 聊天咨询服务启动类
 *
 * @author 稚名不带撇
 */
@Slf4j
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.zmbdp.**.feign"})
public class ZmbdpChatServiceApplication {

    /**
     * 启动方法
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ZmbdpChatServiceApplication.class, args);
        log.info("聊天咨询服务启动成功......");
    }
}
