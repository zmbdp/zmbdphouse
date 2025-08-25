package com.zmbdp.common.security.handler;

import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.common.domain.domain.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

/**
 * WebFlux全局异常处理工具类
 *
 * @author 稚名不带撇
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class GlobalWebFluxExceptionHandler {

    /**
     * 拦截运行时异常
     *
     * @param e        异常信息
     * @param exchange 请求信息
     * @return 响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(
            RuntimeException e,
            ServerWebExchange exchange
    ) {
        String requestURI = exchange.getRequest().getPath().value();
        log.error("请求地址: '{}', 发生运行时异常. ", requestURI, e);
        return Result.fail(ResultCode.ERROR.getCode(), ResultCode.ERROR.getErrMsg());
    }

    /**
     * 系统异常
     *
     * @param e        异常信息
     * @param exchange 服务交换信息
     * @return 响应结果
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, ServerWebExchange exchange) {
        String requestURI = exchange.getRequest().getPath().value();
        log.error("请求地址: '{}', 发生异常. ", requestURI, e);
        return Result.fail(ResultCode.ERROR.getCode(), ResultCode.ERROR.getErrMsg());
    }
}
