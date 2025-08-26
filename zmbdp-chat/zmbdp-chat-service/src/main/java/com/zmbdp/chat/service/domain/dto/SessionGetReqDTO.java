package com.zmbdp.chat.service.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 会话获取参数
 *
 * @author 稚名不带撇
 */
@Data
public class SessionGetReqDTO {

    @NotNull(message = "用户1 id不能为空！")
    private Long userId1;

    @NotNull(message = "用户2 id不能为空！")
    private Long userId2;
}