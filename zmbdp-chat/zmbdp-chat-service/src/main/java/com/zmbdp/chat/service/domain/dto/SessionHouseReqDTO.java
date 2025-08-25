package com.zmbdp.chat.service.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 会话房源映射参数
 *
 * @author 稚名不带撇
 */
@Data
public class SessionHouseReqDTO {

    @NotNull(message = "会话id不能为空！")
    private Long sessionId;

    @NotNull(message = "房源id不能为空！")
    private Long houseId;
}