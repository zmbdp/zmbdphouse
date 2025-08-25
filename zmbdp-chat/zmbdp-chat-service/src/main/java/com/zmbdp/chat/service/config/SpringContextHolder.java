package com.zmbdp.chat.service.config;

import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 静态获取 Spring Bean
 *
 * @author 稚名不带撇
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    /**
     * 上下文
     */
    @Getter
    private static ApplicationContext context;

    /**
     * 获取 Bean
     *
     * @param clazz 类
     * @param <T>   类型
     * @return Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * 获取 Bean
     *
     * @param name  名称
     * @param clazz 类
     * @param <T>   类型
     * @return Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

    /**
     * 设置上下文
     *
     * @param applicationContext 上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.context = applicationContext;
    }
}
